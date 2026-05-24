let currentUser = null;
const statusLabels = {NEW:'新建',ASSIGNED:'已分配',FIXING:'修复中',PENDING_VERIFY:'待验证',CLOSED:'已关闭',REOPENED:'重新打开'};
const statusTones = {NEW:'violet',ASSIGNED:'blue',FIXING:'amber',PENDING_VERIFY:'cyan',CLOSED:'green',REOPENED:'rose'};
const pageMeta = {
  plans: {title: '测试计划', description: '围绕测试生命周期进行统一规划、执行与缺陷闭环。'},
  cases: {title: '测试用例', description: '集中维护用例信息、执行结果与预期对齐情况。'},
  tasks: {title: '测试任务', description: '按计划分配执行任务，跟踪处理状态和截止时间。'},
  defects: {title: '缺陷跟踪', description: '集中查看缺陷状态、责任人和流转动作。'},
  reports: {title: '测试报告', description: '汇总用例执行和缺陷状态，生成测试结论。'},
  users: {title: '用户权限', description: '查看用户、角色和权限分配情况。'},
  logs: {title: '系统日志', description: '查看访问、安全、业务操作和异常日志，并导出日志文件。'}
};
const THEME_KEY = 'tpt-theme';
const pagination = {
  plans: {page: 1, size: 4},
  cases: {page: 1, size: 5},
  tasks: {page: 1, size: 6},
  defects: {page: 1, size: 6},
  users: {page: 1, size: 6}
};
const pageSignatures = {};

const api = async (path, options = {}) => {
  const headers = options.body instanceof FormData ? {} : {'Content-Type':'application/json'};
  const response = await fetch(path, {credentials:'same-origin', headers, ...options});
  const json = await response.json();
  if (!json.success) throw new Error(json.error || '请求失败');
  return json.data;
};

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => {
    registrations.forEach(registration => registration.unregister());
  });
}

applyTheme(loadTheme());

document.getElementById('loginForm').addEventListener('submit', async event => {
  event.preventDefault();
  const data = formData(event.target);
  try {
    currentUser = await api('/api/auth/login', {method:'POST', body:JSON.stringify(data)});
    await initApp();
  } catch (error) { toast(error.message, true); }
});

async function initApp() {
  document.getElementById('loginView').classList.add('hidden');
  document.getElementById('appView').classList.remove('hidden');
  document.getElementById('userInfo').textContent = `${currentUser.realName} · ${currentUser.roles.join(' / ')}`;
  applyPermissions();
  setActiveNav(document.querySelector('.nav-item.active') || document.querySelector('.nav-item'));
  await Promise.all([loadDashboard(), loadPlans(), loadCases(), loadTasks(), loadDefects(), loadUsers()]);
}

async function trySession() {
  try {
    currentUser = await api('/api/auth/me');
    await initApp();
  } catch (_) {
    document.getElementById('loginView').classList.remove('hidden');
    document.getElementById('appView').classList.add('hidden');
  }
}

function applyPermissions() {
  document.querySelectorAll('[data-perm]').forEach(el => {
    el.style.display = currentUser.permissions.includes(el.dataset.perm) ? '' : 'none';
  });
}

async function logout() {
  await api('/api/auth/logout', {method:'POST'});
  location.reload();
}

async function loadDashboard() {
  const data = await api('/api/dashboard');
  const items = [
    ['测试计划', data.planCount, '个', 'rgba(14,165,233,0.28)'],
    ['测试用例', data.caseCount, '条', 'rgba(139,92,246,0.28)'],
    ['测试通过率', data.passRate, '%', 'rgba(16,185,129,0.28)'],
    ['缺陷总数', data.defectCount, '个', 'rgba(244,63,94,0.24)']
  ];
  document.getElementById('metrics').innerHTML = items.map(([label, value, unit, glow]) => `<article class="metric glass glass-medium" style="--metric-bg:${glow}"><span>${label}</span><strong>${value}<em>${unit}</em></strong><i></i></article>`).join('');
}

async function loadPlans() {
  const rows = await api('/api/plans');
  const status = document.getElementById('planStatusFilter')?.value || '';
  const owner = (document.getElementById('planOwnerFilter')?.value || '').trim().toLowerCase();
  const keyword = (document.getElementById('planKeywordFilter')?.value || '').trim().toLowerCase();
  const filtered = rows.filter(row => {
    const matchesStatus = !status || (row.status || '') === status;
    const ownerName = (row.ownerName || '').toLowerCase();
    const text = `${row.name || ''} ${row.objective || ''}`.toLowerCase();
    return matchesStatus && (!owner || ownerName.includes(owner)) && (!keyword || text.includes(keyword));
  });
  renderPlanStats(filtered);
  const page = paginateRows('plans', filtered, `${status}|${owner}|${keyword}`);
  document.getElementById('planList').innerHTML = page.rows.length ? page.rows.map(renderPlanCard).join('') : renderEmpty('暂无匹配的测试计划');
  renderPagination('planPagination', 'plans', page, 'loadPlans');
}

function renderPlanStats(rows) {
  const inProgress = rows.filter(row => (row.status || '') === '进行中').length;
  const done = rows.filter(row => /完成|关闭/.test(row.status || '')).length;
  const scheduled = rows.filter(row => row.startDate || row.endDate).length;
  document.getElementById('planStats').innerHTML = [
    summaryTile('计划数', rows.length, '当前筛选结果'),
    summaryTile('进行中', inProgress, '持续推进'),
    summaryTile('有排期', scheduled, `已完成 ${done}`)
  ].join('');
}

function renderPlanCard(row) {
  return `<article class="item-card plan-card glass glass-soft">
    <div class="item-grid-head">
      <div>
        <strong>${row.name}</strong>
        <p>${row.objective || '暂无目标'}</p>
      </div>
      <span class="meta-chip">${row.status || '未设置状态'}</span>
    </div>
    <div class="item-meta-grid">
      <span>负责人：${row.ownerName || '未指定'}</span>
      <span>范围：${row.scopeText || '未填写'}</span>
      <span>开始：${row.startDate || '未设置'}</span>
      <span>结束：${row.endDate || '未设置'}</span>
    </div>
  </article>`;
}

async function loadCases() {
  const rows = await api('/api/cases');
  const plan = (document.getElementById('casePlanFilter')?.value || '').trim().toLowerCase();
  const result = document.getElementById('caseResultFilter')?.value || '';
  const module = (document.getElementById('caseModuleFilter')?.value || '').trim().toLowerCase();
  const filtered = rows.filter(row => {
    const planName = (row.planName || '').toLowerCase();
    const moduleName = (row.module || '').toLowerCase();
    const matchesPlan = !plan || planName.includes(plan);
    const matchesResult = !result || (row.result || '') === result;
    const matchesModule = !module || moduleName.includes(module);
    return matchesPlan && matchesResult && matchesModule;
  });
  renderCaseStats(filtered);
  const page = paginateRows('cases', filtered, `${plan}|${result}|${module}`);
  document.getElementById('caseList').innerHTML = page.rows.length ? page.rows.map(renderCaseRow).join('') : renderEmpty('暂无匹配的测试用例');
  renderPagination('casePagination', 'cases', page, 'loadCases');
}

function renderCaseStats(rows) {
  const passed = rows.filter(row => row.result === '通过').length;
  const failed = rows.filter(row => row.result === '失败').length;
  const pending = rows.filter(row => row.result === '未执行').length;
  document.getElementById('caseStats').innerHTML = [
    summaryTile('用例数', rows.length, '当前筛选结果'),
    summaryTile('通过', passed, `失败 ${failed}`),
    summaryTile('未执行', pending, '待回归检查')
  ].join('');
}

function renderCaseRow(row) {
  const resultTone = caseResultTone(row.result);
  return `<article class="item-card case-row glass glass-soft">
    <div class="case-main">
      <div class="item-grid-head">
        <div>
          <strong>${row.title}</strong>
          <p>${row.module} · 计划：${row.planName || '未关联计划'}</p>
        </div>
        <span class="result-chip ${resultTone}">${row.result || '未执行'}</span>
      </div>
      <div class="case-meta-grid">
        <span>预期：${row.expected || '未填写'}</span>
        <span>实际：${row.actual || '未填写'}</span>
        <span>执行人：${row.executorName || '未指定'}</span>
        <span>计划：${row.planName || '未指定'}</span>
      </div>
    </div>
    <div class="case-actions">
      <small>步骤：${row.steps || '暂无步骤描述'}</small>
      ${currentUser.permissions.includes('case:execute') ? `<div class="inline-actions"><button class="mini btn-secondary" onclick="executeCase(${row.id},'通过')">通过</button><button class="mini btn-ghost" onclick="executeCase(${row.id},'失败')">失败</button></div>` : ''}
    </div>
  </article>`;
}

async function loadTasks() {
  const rows = await api('/api/tasks');
  const statusFilter = document.getElementById('taskStatusFilter')?.value || '';
  const assigneeFilter = (document.getElementById('taskAssigneeFilter')?.value || '').trim().toLowerCase();
  const planFilter = (document.getElementById('taskPlanFilter')?.value || '').trim().toLowerCase();
  const filtered = rows.filter(row => {
    const assignee = (row.assigneeName || '').toLowerCase();
    const planName = (row.planName || '').toLowerCase();
    const matchesStatus = !statusFilter || (row.status || '') === statusFilter;
    return matchesStatus && (!assigneeFilter || assignee.includes(assigneeFilter)) && (!planFilter || planName.includes(planFilter));
  });
  renderTaskStats(filtered);
  const page = paginateRows('tasks', filtered, `${statusFilter}|${assigneeFilter}|${planFilter}`);
  renderTaskBoard(page.rows);
  renderPagination('taskPagination', 'tasks', page, 'loadTasks');
}

function renderTaskStats(rows) {
  const pending = rows.filter(row => row.status === '待处理').length;
  const progress = rows.filter(row => row.status === '进行中').length;
  const done = rows.filter(row => row.status === '已完成').length;
  document.getElementById('taskStats').innerHTML = [
    summaryTile('任务数', rows.length, '当前筛选结果'),
    summaryTile('进行中', progress, `待处理 ${pending}`),
    summaryTile('已完成', done, '可继续压缩周期')
  ].join('');
}

function renderTaskBoard(rows) {
  const columns = [
    ['待处理', 'pending'],
    ['进行中', 'progress'],
    ['已完成', 'done']
  ];
  document.getElementById('taskList').innerHTML = columns.map(([label, tone]) => renderTaskLane(label, tone, rows.filter(row => (row.status || '待处理') === label))).join('');
}

function renderTaskLane(label, tone, rows) {
  return `<section class="lane glass glass-soft">
    <div class="lane-header">
      <h4>${label}</h4>
      <span class="meta-chip">${rows.length}</span>
    </div>
    <div class="lane-stack">
      ${rows.length ? rows.map(row => renderTaskCard(row, tone)).join('') : renderEmpty('暂无任务', true)}
    </div>
  </section>`;
}

function renderTaskCard(row, tone) {
  const actions = currentUser.permissions.includes('task:update')
    ? `<div class="inline-actions">${row.status !== '进行中' ? `<button class="mini btn-secondary" onclick="updateTask(${row.id},'进行中')">进行中</button>` : ''}${row.status !== '已完成' ? `<button class="mini btn-primary" onclick="updateTask(${row.id},'已完成')">完成</button>` : ''}</div>`
    : '';
  return `<article class="item-card task-card ${tone} glass glass-soft">
    <div class="item-grid-head">
      <div>
        <strong>${row.title}</strong>
        <p>计划：${row.planName || '未关联计划'} · 负责人：${row.assigneeName || '未指定'}</p>
      </div>
      <span class="meta-chip">${row.status || '待处理'}</span>
    </div>
    <div class="item-meta-grid">
      <span>截止：${row.dueDate || '未设置'}</span>
      <span>负责人：${row.assigneeName || '未指定'}</span>
    </div>
    ${actions}
  </article>`;
}

async function loadDefects() {
  const status = document.getElementById('defectStatus')?.value || '';
  const module = document.getElementById('defectModule')?.value || '';
  const rows = await api(`/api/defects?status=${encodeURIComponent(status)}&module=${encodeURIComponent(module)}`);
  const page = paginateRows('defects', rows, `${status}|${module}`);
  document.getElementById('defectList').innerHTML = page.rows.length ? page.rows.map(row => renderDefect(row)).join('') : renderEmpty('暂无匹配的缺陷');
  renderPagination('defectPagination', 'defects', page, 'loadDefects');
}

function renderDefect(row) {
  const tone = statusTones[row.status] || 'blue';
  return `<article class="defect-card ${tone} glass glass-soft">
    <div class="defect-head">
      <div class="defect-header-main">
        <div class="defect-title-line">
          <span>#${row.id}</span>
          <b>${row.module}</b>
          <span class="status ${tone}">${statusLabels[row.status] || row.status}</span>
        </div>
        <h3>${row.title}</h3>
      </div>
      <div class="defect-inline-actions">
        <span class="severity-chip">严重：${row.severity}</span>
        <span class="priority-chip">优先：${row.priority}</span>
      </div>
    </div>
    <p>${row.steps || '暂无复现步骤'}</p>
    <div class="defect-meta">
      <span>负责人：${row.ownerName || '未分配'}</span>
      <span>提交人：${row.reporterName}</span>
      <span>严重程度：${row.severity}</span>
      <span>优先级：${row.priority}</span>
    </div>
    <div class="defect-actions">
      <small>缺陷状态会在流转后自动刷新，并同步更新统计面板。</small>
      <div class="inline-actions">
        <button class="mini btn-ghost" onclick="toggleAttachments(${row.id}, this)">附件 ${row.attachmentCount || 0}</button>
        <button class="mini btn-secondary" onclick="chooseDefectAttachments(${row.id})">上传附件</button>
        ${transitionButtons(row)}
      </div>
    </div>
    <input id="attachmentInput-${row.id}" class="hidden" type="file" multiple onchange="uploadDefectAttachments(${row.id}, this)">
    <div id="attachments-${row.id}" class="attachment-list hidden"></div>
  </article>`;
}

function transitionButtons(row) {
  const map = {NEW:[['ASSIGNED','分配']],ASSIGNED:[['FIXING','开始修复']],FIXING:[['PENDING_VERIFY','提交验证']],PENDING_VERIFY:[['CLOSED','关闭'],['REOPENED','重开']],REOPENED:[['ASSIGNED','重新分配'],['FIXING','继续修复']]};
  return (map[row.status] || []).map(([status, label], index) => `<button class="mini ${index === 0 ? 'btn-primary' : 'btn-secondary'}" onclick="transitionDefect(${row.id},'${status}')">${label}</button>`).join('') || '<small>流程结束</small>';
}

async function loadReport() {
  const planId = Number(document.getElementById('reportPlanId')?.value || 1);
  const data = await api(`/api/report?planId=${planId}`);
  renderReportStats(data);
  document.getElementById('reportBox').innerHTML = renderReportLayout(data);
}

function renderReportStats(data) {
  const passed = sumCounts(data.cases, item => item.result === '通过');
  const failed = sumCounts(data.cases, item => item.result === '失败');
  const defects = sumCounts(data.defects, () => true);
  document.getElementById('reportStats').innerHTML = [
    summaryTile('计划 ID', data.plan?.id || '-', data.plan?.name || '当前报告'),
    summaryTile('通过用例', passed, `失败 ${failed}`),
    summaryTile('缺陷数', defects, '当前计划范围')
  ].join('');
}

function renderReportLayout(data) {
  const caseTotal = sumCounts(data.cases, () => true);
  const defectTotal = sumCounts(data.defects, () => true);
  return `<article class="report-card glass glass-soft">
    <div class="report-section-head">
      <div>
        <p class="eyebrow">Plan Summary</p>
        <h4>${data.plan?.name || '未命名计划'}</h4>
      </div>
      <span class="meta-chip">计划 #${data.plan?.id || '-'}</span>
    </div>
    <div class="report-body">
      <p>${data.conclusion || '暂无结论'}</p>
      <div class="report-inline">
        <small>用例总量：${caseTotal}</small>
        <small>缺陷总量：${defectTotal}</small>
      </div>
    </div>
    <div class="report-distribution">
      ${renderReportBars(data.cases, caseTotal, item => item.result)}
    </div>
  </article>
  <section class="report-side-stack">
    <article class="report-card glass glass-soft">
      <div class="report-section-head"><h4>用例结果分布</h4><span class="meta-chip">Cases</span></div>
      <div class="report-badges">${renderReportBadges(data.cases, item => item.result)}</div>
    </article>
    <article class="report-card glass glass-soft">
      <div class="report-section-head"><h4>缺陷状态分布</h4><span class="meta-chip">Defects</span></div>
      <div class="report-badges">${renderReportBadges(data.defects, item => statusLabels[item.status] || item.status)}</div>
    </article>
  </section>`;
}

function renderReportBars(items, total, keySelector) {
  return items.map(item => {
    const label = keySelector(item);
    const count = Number(item.count || 0);
    const width = total ? Math.max(8, Math.round((count / total) * 100)) : 8;
    return `<div class="report-bar"><div class="item-grid-head"><strong>${label}</strong><small>${count}</small></div><div class="report-bar-track"><div class="report-bar-fill" style="width:${width}%"></div></div></div>`;
  }).join('');
}

function renderReportBadges(items, keySelector) {
  return items.map(item => `<div class="report-badge"><span>${keySelector(item)}</span><strong>${item.count || 0}</strong></div>`).join('');
}

async function loadUsers() {
  if (!currentUser?.permissions.includes('user:manage')) return;
  const rows = await api('/api/users');
  const page = paginateRows('users', rows, 'all');
  document.getElementById('userList').innerHTML = page.rows.length ? page.rows.map(row => `<article class="item-card glass glass-soft"><div class="item-top"><strong>${row.realName}</strong><span>${row.username}</span></div><p>角色：${row.roles || '未分配'}</p><small>状态：${row.enabled ? '启用' : '禁用'}</small><div class="inline-actions"><button class="mini btn-secondary" onclick='editUser(${JSON.stringify(row)})'>编辑</button><button class="mini btn-ghost" onclick="toggleUser(${row.id}, ${!row.enabled})">${row.enabled ? '禁用' : '启用'}</button><button class="mini btn-primary" onclick="resetUserPassword(${row.id})">重置密码</button></div></article>`).join('') : renderEmpty('暂无用户');
  renderPagination('userPagination', 'users', page, 'loadUsers');
}

async function loadLogs() {
  if (!currentUser?.permissions.includes('user:manage')) return;
  const lines = Number(document.getElementById('logLineCount')?.value || 200);
  const data = await api(`/api/logs?lines=${lines}`);
  const logLines = data.lines || [];
  document.getElementById('logStats').innerHTML = [
    summaryTile('日志文件', data.file || 'logs/test-process-tracker.log', '本地运行目录'),
    summaryTile('总行数', data.totalLines || 0, '当前文件'),
    summaryTile('已显示', logLines.length, `最近 ${lines} 行`)
  ].join('');
  document.getElementById('logViewer').textContent = logLines.length ? logLines.join('\n') : '暂无日志内容。';
}

async function submitPlan(e) { await submit(e, '/api/plans', loadPlans, '计划已创建'); await loadDashboard(); }
async function submitCase(e) { await submit(e, '/api/cases', loadCases, '用例已创建'); await loadDashboard(); }
async function submitTask(e) { await submit(e, '/api/tasks', loadTasks, '任务已分配'); await loadDashboard(); }
async function submitDefect(e) {
  e.preventDefault();
  try {
    const data = formData(e.target);
    delete data.attachments;
    const defect = await api('/api/defects', {method:'POST', body:JSON.stringify(data)});
    const files = e.target.attachments?.files || [];
    if (files.length) {
      const form = new FormData();
      [...files].forEach(file => form.append('files', file));
      await api(`/api/defects/${defect.id}/attachments`, {method:'POST', body:form});
    }
    closeModal('defectModal');
    e.target.reset();
    toast(files.length ? '缺陷和附件已提交' : '缺陷已提交');
    await loadDefects();
    await loadDashboard();
  } catch (error) { toast(error.message, true); }
}
async function submitUser(e) {
  e.preventDefault();
  const data = formData(e.target);
  const editing = Boolean(data.id);
  if (editing && !data.password) delete data.password;
  try {
    await api(editing ? `/api/users/${data.id}` : '/api/users', {method: editing ? 'PUT' : 'POST', body: JSON.stringify(data)});
    closeModal('userModal');
    e.target.reset();
    document.getElementById('userModalTitle').textContent = '新增用户';
    toast(editing ? '用户已更新' : '用户已创建');
    await loadUsers();
  } catch (error) { toast(error.message, true); }
}

async function submit(e, path, reload, message) {
  e.preventDefault();
  try {
    await api(path, {method:'POST', body:JSON.stringify(formData(e.target))});
    closeModal(e.target.closest('.modal').id);
    e.target.reset();
    toast(message);
    await reload();
  } catch (error) { toast(error.message, true); }
}

async function executeCase(id, result) {
  await api(`/api/cases/${id}/execute`, {method:'PUT', body:JSON.stringify({actual: result === '通过' ? '执行通过' : '执行失败', result, executorId: currentUser.id})});
  toast('用例执行结果已更新'); await loadCases(); await loadDashboard();
}

async function updateTask(id, status) {
  await api(`/api/tasks/${id}/status`, {method:'PUT', body:JSON.stringify({status})});
  toast('任务状态已更新'); await loadTasks();
}

async function transitionDefect(id, status) {
  try {
    const ownerId = status === 'ASSIGNED' ? 5 : null;
    await api(`/api/defects/${id}/transition`, {method:'POST', body:JSON.stringify({status, ownerId, note:'页面操作流转'})});
    toast('缺陷状态已流转'); await loadDefects(); await loadDashboard();
  } catch (error) { toast(error.message, true); }
}

async function toggleAttachments(defectId, button) {
  const box = document.getElementById(`attachments-${defectId}`);
  if (!box) return;
  if (!box.classList.contains('hidden')) {
    box.classList.add('hidden');
    return;
  }
  const rows = await api(`/api/defects/${defectId}/attachments`);
  box.innerHTML = rows.length ? rows.map(renderAttachment).join('') : '<small>暂无附件</small>';
  box.classList.remove('hidden');
  if (button) button.textContent = `附件 ${rows.length}`;
}

function chooseDefectAttachments(defectId) {
  document.getElementById(`attachmentInput-${defectId}`)?.click();
}

async function uploadDefectAttachments(defectId, input) {
  const files = input.files || [];
  if (!files.length) return;
  try {
    const form = new FormData();
    [...files].forEach(file => form.append('files', file));
    const rows = await api(`/api/defects/${defectId}/attachments`, {method:'POST', body:form});
    input.value = '';
    const box = document.getElementById(`attachments-${defectId}`);
    if (box) {
      box.innerHTML = rows.length ? rows.map(renderAttachment).join('') : '<small>暂无附件</small>';
      box.classList.remove('hidden');
    }
    toast('附件已上传');
    await loadDefects();
  } catch (error) {
    input.value = '';
    toast(error.message, true);
  }
}

function renderAttachment(row) {
  const size = formatFileSize(row.fileSize || 0);
  return `<a class="attachment-item" href="/api/defects/attachments/${row.id}/download" target="_blank" rel="noopener">
    <span>${row.originalName}</span><small>${size} · ${row.uploadedBy || '未知用户'}</small>
  </a>`;
}

function formatFileSize(bytes) {
  const value = Number(bytes || 0);
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  return `${(value / 1024 / 1024).toFixed(1)} MB`;
}

function editUser(row) {
  const form = document.querySelector('#userModal form');
  document.getElementById('userModalTitle').textContent = '编辑用户';
  form.id.value = row.id;
  form.username.value = row.username;
  form.username.readOnly = true;
  form.password.value = '';
  form.realName.value = row.realName || '';
  form.roleId.value = row.roleId || '';
  form.enabled.value = String(Boolean(row.enabled));
  openModal('userModal');
}

async function toggleUser(id, enabled) {
  await api(`/api/users/${id}/enabled`, {method:'PATCH', body:JSON.stringify({enabled})});
  toast(enabled ? '用户已启用' : '用户已禁用');
  await loadUsers();
}

async function resetUserPassword(id) {
  const password = prompt('请输入新密码', 'password');
  if (!password) return;
  await api(`/api/users/${id}/password`, {method:'PATCH', body:JSON.stringify({password})});
  toast('密码已重置为 BCrypt 加密存储');
}

function exportReport(format) {
  const planId = Number(document.getElementById('reportPlanId')?.value || 1);
  window.location.href = `/api/report/export?planId=${planId}&format=${format}`;
}

function exportLogs() {
  window.location.href = '/api/logs/export';
}

function summaryTile(label, value, helper) {
  return `<article class="summary-tile glass glass-soft"><span>${label}</span><strong>${value}</strong><small>${helper}</small></article>`;
}

function renderEmpty(message, compact=false) {
  return `<article class="item-card glass glass-soft${compact ? ' compact-empty' : ''}"><p>${message}</p></article>`;
}

function paginateRows(key, rows, signature) {
  const state = pagination[key];
  if (pageSignatures[key] !== signature) {
    pageSignatures[key] = signature;
    state.page = 1;
  }
  const total = rows.length;
  const totalPages = Math.max(1, Math.ceil(total / state.size));
  state.page = Math.min(Math.max(1, state.page), totalPages);
  const start = (state.page - 1) * state.size;
  return {
    rows: rows.slice(start, start + state.size),
    page: state.page,
    size: state.size,
    total,
    totalPages,
    from: total ? start + 1 : 0,
    to: Math.min(start + state.size, total)
  };
}

function renderPagination(containerId, key, page, loaderName) {
  const container = document.getElementById(containerId);
  if (!container) return;
  const sizes = [4, 5, 6, 10, 20];
  container.innerHTML = `<div class="pagination-info">显示 ${page.from}-${page.to} / 共 ${page.total} 条，第 ${page.page} / ${page.totalPages} 页</div>
    <div class="pagination-actions">
      <select onchange="changePageSize('${key}', this.value, '${loaderName}')">${sizes.map(size => `<option value="${size}" ${size === page.size ? 'selected' : ''}>每页 ${size} 条</option>`).join('')}</select>
      <button class="mini btn-secondary" onclick="changePage('${key}', -1, '${loaderName}')" ${page.page <= 1 ? 'disabled' : ''}>上一页</button>
      <button class="mini btn-secondary" onclick="changePage('${key}', 1, '${loaderName}')" ${page.page >= page.totalPages ? 'disabled' : ''}>下一页</button>
    </div>`;
}

function changePage(key, delta, loaderName) {
  pagination[key].page += delta;
  window[loaderName]();
}

function changePageSize(key, size, loaderName) {
  pagination[key].size = Number(size);
  pagination[key].page = 1;
  window[loaderName]();
}

function caseResultTone(result) {
  if (result === '通过') return 'pass';
  if (result === '失败') return 'fail';
  if (result === '阻塞') return 'blocked';
  return 'pending';
}

function sumCounts(items, predicate) {
  return (items || []).filter(predicate).reduce((sum, item) => sum + Number(item.count || 0), 0);
}

function formData(form) {
  return Object.fromEntries([...new FormData(form).entries()].map(([k, v]) => [k, v === '' ? null : isNumericField(k) ? Number(v) : v]));
}

function isNumericField(key) { return ['id','planId','caseId','ownerId','executorId','assigneeId','roleId'].includes(key); }
function openModal(id) { document.getElementById(id).classList.add('show'); }
function closeModal(id) {
  document.getElementById(id).classList.remove('show');
  if (id === 'userModal') {
    const form = document.querySelector('#userModal form');
    form.reset();
    form.id.value = '';
    form.username.readOnly = false;
    document.getElementById('userModalTitle').textContent = '新增用户';
  }
}

function switchTab(id, btn) {
  document.querySelectorAll('.tab-panel').forEach(x => x.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(x => x.classList.remove('active'));
  document.getElementById(id).classList.add('active');
  setActiveNav(btn);
  if (id === 'reports') loadReport();
  if (id === 'logs') loadLogs();
}

function setActiveNav(btn) {
  if (!btn) return;
  btn.classList.add('active');
  const id = btn.getAttribute('onclick')?.match(/'([^']+)'/)?.[1];
  const meta = pageMeta[id] || pageMeta.plans;
  const title = document.getElementById('pageTitle');
  const description = document.getElementById('pageDescription');
  if (title) title.textContent = meta.title;
  if (description) description.textContent = meta.description;
}

function toast(message, error=false) {
  const el = document.getElementById('toast');
  el.textContent = message;
  el.className = `toast show ${error ? 'error' : ''}`;
  setTimeout(() => el.className = 'toast', 2200);
}

function loadTheme() {
  return localStorage.getItem(THEME_KEY) || 'dark';
}

function applyTheme(theme) {
  document.body.dataset.theme = theme;
  const nextLabel = theme === 'dark' ? '切换浅色' : '切换深色';
  const loginLabel = document.getElementById('themeToggleLabelLogin');
  const appLabel = document.getElementById('themeToggleLabelApp');
  if (loginLabel) loginLabel.textContent = nextLabel;
  if (appLabel) appLabel.textContent = nextLabel;
}

function toggleTheme() {
  const next = document.body.dataset.theme === 'dark' ? 'light' : 'dark';
  localStorage.setItem(THEME_KEY, next);
  applyTheme(next);
  toast(next === 'dark' ? '已切换为深色模式' : '已切换为浅色模式');
}

trySession();
