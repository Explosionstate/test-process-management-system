package com.example.tracker.service;

import com.example.tracker.auth.LoginUser;
import com.example.tracker.common.BusinessException;
import com.example.tracker.domain.DefectRequest;
import com.example.tracker.domain.TransitionRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DefectService {
    private static final Logger log = LoggerFactory.getLogger(DefectService.class);
    private final JdbcTemplate jdbc;
    private final Path uploadDir;

    public DefectService(JdbcTemplate jdbc, @Value("${app.upload-dir:uploads/defects}") String uploadDir) {
        this.jdbc = jdbc;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public List<Map<String, Object>> list(String status, String module, Long ownerId) {
        return jdbc.queryForList("""
                select d.id,d.title,d.module,d.severity,d.priority,d.steps,d.expected,d.actual,d.status,
                       reporter.real_name reporterName,owner.real_name ownerName,d.created_at createdAt,d.updated_at updatedAt,
                       count(a.id) attachmentCount
                from defect d join sys_user reporter on d.reporter_id=reporter.id left join sys_user owner on d.owner_id=owner.id
                left join defect_attachment a on a.defect_id=d.id
                where (? is null or d.status=?) and (? is null or d.module like concat('%',?,'%')) and (? is null or d.owner_id=?)
                group by d.id,d.title,d.module,d.severity,d.priority,d.steps,d.expected,d.actual,d.status,
                         reporter.real_name,owner.real_name,d.created_at,d.updated_at
                order by d.id desc
                """, emptyToNull(status), emptyToNull(status), emptyToNull(module), emptyToNull(module), ownerId, ownerId);
    }

    @Transactional
    public Map<String, Object> create(DefectRequest request, LoginUser user) {
        if (request.title() == null || request.title().isBlank() || request.module() == null || request.module().isBlank()) {
            throw new BusinessException("缺陷标题和所属模块不能为空");
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("insert into defect(title,module,severity,priority,steps,expected,actual,status,reporter_id,owner_id) values(?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.title());
            ps.setString(2, request.module());
            ps.setString(3, value(request.severity(), "中"));
            ps.setString(4, value(request.priority(), "中"));
            ps.setString(5, request.steps());
            ps.setString(6, request.expected());
            ps.setString(7, request.actual());
            ps.setString(8, "NEW");
            ps.setLong(9, user.id());
            if (request.ownerId() == null) ps.setNull(10, Types.BIGINT); else ps.setLong(10, request.ownerId());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        jdbc.update("insert into defect_history(defect_id,from_status,to_status,operator_id,note) values(?,?,?,?,?)", id, null, "NEW", user.id(), "缺陷创建");
        log.info("defect_created id={} title={} module={} reporter={} ownerId={}", id, request.title(), request.module(), user.username(), request.ownerId());
        return jdbc.queryForMap("select * from defect where id=?", id);
    }

    @Transactional
    public void transition(Long id, TransitionRequest request, LoginUser user) {
        Map<String, Object> defect = jdbc.queryForList("select * from defect where id=?", id).stream()
                .findFirst().orElseThrow(() -> new BusinessException("缺陷不存在"));
        String current = String.valueOf(defect.get("status"));
        String next = request.status();
        if (!canTransition(current, next)) {
            throw new BusinessException("非法状态流转：" + current + " -> " + next);
        }
        requireTransitionPermission(user, next);
        jdbc.update("update defect set status=?, owner_id=coalesce(?, owner_id), updated_at=now() where id=?", next, request.ownerId(), id);
        jdbc.update("insert into defect_history(defect_id,from_status,to_status,operator_id,note) values(?,?,?,?,?)",
                id, current, next, user.id(), request.note());
        log.info("defect_transition defectId={} from={} to={} operator={} ownerId={} note={}", id, current, next, user.username(), request.ownerId(), request.note());
    }

    public List<Map<String, Object>> history(Long id) {
        return jdbc.queryForList("""
                select h.id,h.defect_id defectId,h.from_status fromStatus,h.to_status toStatus,u.real_name operatorName,h.note,h.operated_at operatedAt
                from defect_history h join sys_user u on h.operator_id=u.id where h.defect_id=? order by h.id
                """, id);
    }

    @Transactional
    public List<Map<String, Object>> uploadAttachments(Long defectId, MultipartFile[] files, LoginUser user) throws IOException {
        ensureDefectExists(defectId);
        if (files == null || files.length == 0) {
            return attachments(defectId);
        }
        Files.createDirectories(uploadDir);
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String original = cleanFilename(file.getOriginalFilename());
            String stored = UUID.randomUUID() + extension(original);
            Path target = uploadDir.resolve(stored).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new BusinessException("附件文件名非法");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("insert into defect_attachment(defect_id,original_name,stored_name,file_path,content_type,file_size,uploaded_by) values(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, defectId);
                ps.setString(2, original);
                ps.setString(3, stored);
                ps.setString(4, uploadDir.relativize(target).toString());
                ps.setString(5, file.getContentType());
                ps.setLong(6, file.getSize());
                ps.setLong(7, user.id());
                return ps;
            }, keyHolder);
            log.info("defect_attachment_uploaded defectId={} attachmentId={} originalName={} size={} user={}", defectId, keyHolder.getKey().longValue(), original, file.getSize(), user.username());
        }
        return attachments(defectId);
    }

    public List<Map<String, Object>> attachments(Long defectId) {
        ensureDefectExists(defectId);
        return jdbc.queryForList("""
                select a.id,a.defect_id defectId,a.original_name originalName,a.content_type contentType,a.file_size fileSize,
                       u.real_name uploadedBy,a.uploaded_at uploadedAt
                from defect_attachment a join sys_user u on a.uploaded_by=u.id
                where a.defect_id=? order by a.id desc
                """, defectId);
    }

    public AttachmentFile loadAttachment(Long attachmentId) throws MalformedURLException {
        Map<String, Object> attachment = jdbc.queryForList("select * from defect_attachment where id=?", attachmentId).stream()
                .findFirst().orElseThrow(() -> new BusinessException("附件不存在"));
        Path file = uploadDir.resolve(String.valueOf(attachment.get("stored_name"))).normalize();
        if (!file.startsWith(uploadDir) || !Files.exists(file)) {
            throw new BusinessException("附件文件不存在");
        }
        Resource resource = new UrlResource(file.toUri());
        String contentType = String.valueOf(attachment.get("content_type"));
        MediaType mediaType = contentType == null || contentType.isBlank() || "null".equals(contentType)
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
        return new AttachmentFile(resource, String.valueOf(attachment.get("original_name")), mediaType);
    }

    private void ensureDefectExists(Long defectId) {
        Integer count = jdbc.queryForObject("select count(*) from defect where id=?", Integer.class, defectId);
        if (count == null || count == 0) {
            throw new BusinessException("缺陷不存在");
        }
    }

    private static String cleanFilename(String filename) {
        String value = filename == null || filename.isBlank() ? "attachment" : Path.of(filename).getFileName().toString();
        return value.replaceAll("[\\r\\n]", "_");
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    public record AttachmentFile(Resource resource, String filename, MediaType mediaType) {
    }

    private static boolean canTransition(String current, String next) {
        return switch (current) {
            case "NEW" -> "ASSIGNED".equals(next);
            case "ASSIGNED" -> "FIXING".equals(next);
            case "FIXING" -> "PENDING_VERIFY".equals(next);
            case "PENDING_VERIFY" -> "CLOSED".equals(next) || "REOPENED".equals(next);
            case "REOPENED" -> "ASSIGNED".equals(next) || "FIXING".equals(next);
            default -> false;
        };
    }

    private static void requireTransitionPermission(LoginUser user, String next) {
        String permission = switch (next) {
            case "ASSIGNED" -> "defect:assign";
            case "FIXING", "PENDING_VERIFY" -> "defect:fix";
            case "CLOSED", "REOPENED" -> "defect:verify";
            default -> "defect:view";
        };
        if (!user.has(permission)) {
            throw new SecurityException("缺少权限：" + permission);
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
