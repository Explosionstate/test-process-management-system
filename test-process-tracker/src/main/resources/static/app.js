let currentUser = null;
const statusLabels = {NEW:'新建',ASSIGNED:'已分配',FIXING:'修复中',PENDING_VERIFY:'待验证',CLOSED:'已关闭',REOPENED:'重新打开'};
const statusTones = {NEW:'violet',ASSIGNED:'blue',FIXING:'amber',PENDING_VERIFY:'cyan',CLOSED:'green',REOPENED:'rose'};

const api = async (path, options = {}) => {
  const response = await fetch(path, {credentials:'same-origin', headers:{'Content-Type':'application/json'}, ...options});
  const json = await response.json();
  if (!json.success) throw new Error(json.error || '请求失败');
  return json.data;
};

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => {
    registrations.forEach(registration => registration.unregister());
  });
}

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
  const items = [['测试计划',data.planCount,'个'],['测试用例',data.caseCount,'条'],['测试通过率',data.passRate,'%'],['缺陷总数',data.defectCount,'个']];
  document.getElementById('metrics').innerHTML = items.map(([label,value,unit]) => `<article class="metric glass"><span>${label}</span><strong>${value}<em>${unit}</em></strong><i></i></article>`).join('');
}

async function loadPlans() {
  const rows = await api('/api/plans');
  document.getElementById('planList').innerHTML = rows.map(row => `<article class="item-card"><div class="item-top"><strong>${row.name}</strong><span>${row.status}</span></div><p>${row.objective || '暂无目标'}</p><small>负责人：${row.ownerName || '未指定'} · 范围：${row.scopeText || '未填写'} · ${row.startDate || ''} 至 ${row.endDate || ''}</small></article>`).join('');
}

async function loadCases() {
  const rows = await api('/api/cases');
  document.getElementById('caseList').innerHTML = rows.map(row => `<article class="item-card"><div class="item-top"><strong>${row.title}</strong><span>${row.result}</span></div><p>${row.module} · 计划：${row.planName}</p><small>预期：${row.expected || ''} · 实际：${row.actual || ''} · 执行人：${row.executorName || '未指定'}</small>${currentUser.permissions.includes('case:execute') ? `<div class="inline-actions"><button class="mini" onclick="executeCase(${row.id},'通过')">通过</button><button class="mini" onclick="executeCase(${row.id},'失败')">失败</button></div>` : ''}</article>`).join('');
}

async function loadTasks() {
  const rows = await api('/api/tasks');
  document.getElementById('taskList').innerHTML = rows.map(row => `<article class="item-card"><div class="item-top"><strong>${row.title}</strong><span>${row.status}</span></div><p>计划：${row.planName} · 负责人：${row.assigneeName}</p><small>截止日期：${row.dueDate || '未设置'}</small>${currentUser.permissions.includes('task:update') ? `<div class="inline-actions"><button class="mini" onclick="updateTask(${row.id},'进行中')">进行中</button><button class="mini" onclick="updateTask(${row.id},'已完成')">完成</button></div>` : ''}</article>`).join('');
}

async function loadDefects() {
  const status = document.getElementById('defectStatus')?.value || '';
  const module = document.getElementById('defectModule')?.value || '';
  const rows = await api(`/api/defects?status=${encodeURIComponent(status)}&module=${encodeURIComponent(module)}`);
  document.getElementById('defectList').innerHTML = rows.map(row => renderDefect(row)).join('');
}

function renderDefect(row) {
  const tone = statusTones[row.status] || 'blue';
  return `<article class="defect-card ${tone}"><div class="defect-head"><span>#${row.id}</span><b>${row.module}</b></div><h3>${row.title}</h3><p>${row.steps || '暂无复现步骤'}</p><div class="defect-meta"><span>严重：${row.severity}</span><span>优先：${row.priority}</span><span>负责人：${row.ownerName || '未分配'}</span><span>提交人：${row.reporterName}</span></div><div class="defect-actions"><span class="status ${tone}">${statusLabels[row.status] || row.status}</span><div>${transitionButtons(row)}</div></div></article>`;
}

function transitionButtons(row) {
  const map = {NEW:[['ASSIGNED','分配']],ASSIGNED:[['FIXING','开始修复']],FIXING:[['PENDING_VERIFY','提交验证']],PENDING_VERIFY:[['CLOSED','关闭'],['REOPENED','重开']],REOPENED:[['ASSIGNED','重新分配'],['FIXING','继续修复']]};
  return (map[row.status] || []).map(([status,label]) => `<button class="mini" onclick="transitionDefect(${row.id},'${status}')">${label}</button>`).join('') || '<small>流程结束</small>';
}

async function loadReport() {
  const data = await api('/api/report?planId=1');
  document.getElementById('reportBox').innerHTML = `<article class="item-card"><h3>${data.plan.name}</h3><p>${data.conclusion}</p><small>用例统计：${data.cases.map(x => `${x.result}:${x.count}`).join('，')} · 缺陷统计：${data.defects.map(x => `${statusLabels[x.status] || x.status}:${x.count}`).join('，')}</small></article>`;
}

async function loadUsers() {
  if (!currentUser?.permissions.includes('user:manage')) return;
  const rows = await api('/api/users');
  document.getElementById('userList').innerHTML = rows.map(row => `<article class="item-card"><div class="item-top"><strong>${row.realName}</strong><span>${row.username}</span></div><p>角色：${row.roles || '未分配'}</p><small>状态：${row.enabled ? '启用' : '禁用'}</small></article>`).join('');
}

async function submitPlan(e) { await submit(e, '/api/plans', loadPlans, '计划已创建'); await loadDashboard(); }
async function submitCase(e) { await submit(e, '/api/cases', loadCases, '用例已创建'); await loadDashboard(); }
async function submitTask(e) { await submit(e, '/api/tasks', loadTasks, '任务已分配'); }
async function submitDefect(e) { await submit(e, '/api/defects', loadDefects, '缺陷已提交'); await loadDashboard(); }
async function submitUser(e) { await submit(e, '/api/users', loadUsers, '用户已创建'); }

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

function formData(form) {
  return Object.fromEntries([...new FormData(form).entries()].map(([k,v]) => [k, v === '' ? null : isNumericField(k) ? Number(v) : v]));
}

function isNumericField(key) { return ['id','planId','caseId','ownerId','executorId','assigneeId','roleId'].includes(key); }
function openModal(id) { document.getElementById(id).classList.add('show'); }
function closeModal(id) { document.getElementById(id).classList.remove('show'); }
function switchTab(id, btn) { document.querySelectorAll('.tab-panel').forEach(x => x.classList.remove('active')); document.querySelectorAll('.tabs button').forEach(x => x.classList.remove('active')); document.getElementById(id).classList.add('active'); btn.classList.add('active'); if (id === 'reports') loadReport(); }
function toast(message, error=false) { const el=document.getElementById('toast'); el.textContent=message; el.className=`toast show ${error?'error':''}`; setTimeout(()=>el.className='toast',2200); }

trySession();
