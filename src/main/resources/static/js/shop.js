const state = {
  products: [],
  customer: null,
  authMode: 'login',
  selectedProduct: null
};

const $ = (selector) => document.querySelector(selector);
const money = (value) => `${Number(value).toLocaleString('ko-KR')} P`;
const escapeHtml = (value = '') => String(value)
  .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;').replaceAll("'", '&#039;');

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  });
  const data = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || `요청 처리에 실패했습니다. (${response.status})`);
  return data;
}

function toast(message, error = false) {
  const el = $('#toast');
  el.textContent = message;
  el.className = `toast show${error ? ' error' : ''}`;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => el.className = 'toast', 2800);
}

async function loadProducts() {
  $('#productGrid').innerHTML = '<div class="loading">상품을 불러오는 중입니다.</div>';
  try {
    state.products = await api('/api/products');
    renderProducts();
    renderAdminProducts();
  } catch (error) {
    $('#productGrid').innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`;
    toast(error.message, true);
  }
}

function renderProducts() {
  const grid = $('#productGrid');
  if (!state.products.length) {
    grid.innerHTML = '<div class="empty">등록된 상품이 없습니다.</div>';
    return;
  }
  grid.innerHTML = state.products.map((product, index) => `
    <article class="product-card">
      <div class="product-visual"><span>${escapeHtml(product.name.slice(0, 1))}</span></div>
      <div class="product-info">
        <span class="product-index">ITEM ${String(index + 1).padStart(2, '0')}</span>
        <h3>${escapeHtml(product.name)}</h3>
        <div class="product-bottom">
          <span class="price">${money(product.price)}</span>
          <button class="button button-dark small" data-order="${product.id}">주문하기</button>
        </div>
      </div>
    </article>`).join('');
}

function renderAdminProducts() {
  const list = $('#adminProductList');
  $('#adminProductCount').textContent = state.products.length;
  if (!state.products.length) {
    list.innerHTML = '<div class="empty">등록된 상품이 없습니다.</div>';
    return;
  }
  list.innerHTML = state.products.map(product => `
    <div class="admin-item">
      <span class="admin-number">${product.id}</span>
      <div class="admin-item-info"><strong>${escapeHtml(product.name)}</strong><small>${money(product.price)}</small></div>
      <div class="admin-actions">
        <button class="button button-outline small" data-edit="${product.id}">수정</button>
        <button class="button button-danger small" data-delete="${product.id}">삭제</button>
      </div>
    </div>`).join('');
}

function renderUser() {
  const area = $('#userArea');
  if (!state.customer) {
    area.innerHTML = '<button class="button button-ghost" id="openSignup">회원가입</button><button class="button button-dark" id="openLogin">로그인</button>';
    bindAuthButtons();
    return;
  }
  area.innerHTML = `
    <div class="user-chip">
      <span class="user-avatar">${escapeHtml(state.customer.name.slice(0, 1))}</span>
      <span class="user-meta"><strong>${escapeHtml(state.customer.name)}</strong><small>${money(state.customer.point)}</small></span>
    </div>
    <button class="button button-ghost small" id="editProfile">내 정보</button>
    <button class="button button-dark small" id="logout">로그아웃</button>`;
  $('#logout').addEventListener('click', logout);
  $('#editProfile').addEventListener('click', openProfile);
}

function bindAuthButtons() {
  $('#openLogin')?.addEventListener('click', () => openAuth('login'));
  $('#openSignup')?.addEventListener('click', () => openAuth('signup'));
}

function openAuth(mode) {
  state.authMode = mode;
  const signup = mode === 'signup';
  $('#authTitle').textContent = signup ? '회원가입' : '로그인';
  $('#authDescription').textContent = signup ? '가입 즉시 100,000 포인트를 드려요.' : '포인트와 주문 내역을 확인해보세요.';
  $('#authSubmit').textContent = signup ? '회원가입' : '로그인';
  $('#nameField').classList.toggle('hidden', !signup);
  $('#authName').required = signup;
  $('#authPassword').autocomplete = signup ? 'new-password' : 'current-password';
  $('#authForm').reset();
  $('#authDialog').showModal();
}

async function submitAuth(event) {
  event.preventDefault();
  const customerId = $('#authCustomerId').value.trim();
  const password = $('#authPassword').value;
  try {
    if (state.authMode === 'signup') {
      await api('/api/customers', { method: 'POST', body: JSON.stringify({ customerId, name: $('#authName').value.trim(), password }) });
      toast('회원가입이 완료됐습니다. 로그인해 주세요.');
      openAuth('login');
      $('#authCustomerId').value = customerId;
      return;
    }
    await api('/api/customers/login', { method: 'POST', body: JSON.stringify({ customerId, password }) });
    state.customer = await api(`/api/customers/${encodeURIComponent(customerId)}`);
    localStorage.setItem('skalaCustomerId', customerId);
    $('#authDialog').close();
    renderUser();
    toast(`${state.customer.name}님, 반갑습니다.`);
  } catch (error) { toast(error.message, true); }
}

async function restoreSession() {
  const customerId = localStorage.getItem('skalaCustomerId');
  if (!customerId) return;
  try {
    state.customer = await api(`/api/customers/${encodeURIComponent(customerId)}`);
    renderUser();
  } catch (_) { localStorage.removeItem('skalaCustomerId'); }
}

function logout() {
  state.customer = null;
  localStorage.removeItem('skalaCustomerId');
  renderUser();
  showView('products');
  toast('로그아웃했습니다.');
}

function openOrder(productId) {
  if (!state.customer) {
    toast('주문하려면 먼저 로그인해 주세요.', true);
    openAuth('login');
    return;
  }
  state.selectedProduct = state.products.find(product => product.id === Number(productId));
  $('#orderProductId').value = state.selectedProduct.id;
  $('#orderProductName').textContent = state.selectedProduct.name;
  $('#orderProductPrice').textContent = `상품 가격 ${money(state.selectedProduct.price)}`;
  $('#orderQuantity').value = 1;
  updateOrderTotal();
  $('#orderDialog').showModal();
}

function updateOrderTotal() {
  const quantity = Math.max(1, Number($('#orderQuantity').value) || 1);
  $('#orderTotal').textContent = money((state.selectedProduct?.price || 0) * quantity);
}

async function submitOrder(event) {
  event.preventDefault();
  try {
    const result = await api('/api/customers/order', {
      method: 'POST',
      body: JSON.stringify({ customerId: state.customer.customerId, productId: Number($('#orderProductId').value), quantity: Number($('#orderQuantity').value) })
    });
    state.customer = await api(`/api/customers/${encodeURIComponent(state.customer.customerId)}`);
    renderUser();
    $('#orderDialog').close();
    toast(result.message);
  } catch (error) { toast(error.message, true); }
}

async function loadOrders() {
  const container = $('#orderContent');
  if (!state.customer) {
    container.innerHTML = '<div class="empty">로그인하면 주문 내역을 확인할 수 있습니다.<br><br><button class="button button-dark" id="ordersLogin">로그인</button></div>';
    $('#ordersLogin').addEventListener('click', () => openAuth('login'));
    return;
  }
  container.innerHTML = '<div class="loading">주문 내역을 불러오는 중입니다.</div>';
  try {
    state.customer = await api(`/api/customers/${encodeURIComponent(state.customer.customerId)}`);
    renderUser();
    const orders = state.customer.orders || [];
    container.innerHTML = `
      <div class="account-summary"><span class="user-avatar">${escapeHtml(state.customer.name.slice(0, 1))}</span><strong>${escapeHtml(state.customer.name)}님의 주문</strong><span>${money(state.customer.point)}</span></div>
      ${orders.length ? `<div class="orders-list">${orders.map(order => `
        <article class="order-item">
          <div><h3>${escapeHtml(order.productName)}</h3><p>주문 #${order.orderId} · ${order.quantity}개 · 개당 ${money(order.unitPrice)}</p></div>
          <div class="order-price"><strong>${money(order.totalPrice)}</strong><small>총 주문 금액</small></div>
          <button class="button button-outline small" data-cancel-order="${order.productId}" data-max="${order.quantity}">주문 취소</button>
        </article>`).join('')}</div>` : '<div class="empty">아직 주문한 상품이 없습니다.</div>'}`;
  } catch (error) { container.innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`; }
}

async function cancelOrder(productId, maxQuantity) {
  const raw = prompt(`취소할 수량을 입력하세요. (최대 ${maxQuantity}개)`, String(maxQuantity));
  if (raw === null) return;
  const quantity = Number(raw);
  if (!Number.isInteger(quantity) || quantity < 1 || quantity > maxQuantity) {
    toast(`1~${maxQuantity} 사이의 수량을 입력하세요.`, true);
    return;
  }
  try {
    const result = await api('/api/customers/cancel', { method: 'POST', body: JSON.stringify({ customerId: state.customer.customerId, productId: Number(productId), quantity }) });
    toast(result.message);
    await loadOrders();
  } catch (error) { toast(error.message, true); }
}

async function submitProduct(event) {
  event.preventDefault();
  const id = $('#editingProductId').value;
  const body = JSON.stringify({ name: $('#productName').value.trim(), price: Number($('#productPrice').value) });
  try {
    await api(id ? `/api/products/${id}` : '/api/products', { method: id ? 'PUT' : 'POST', body });
    toast(id ? '상품을 수정했습니다.' : '상품을 등록했습니다.');
    resetProductForm();
    await loadProducts();
  } catch (error) { toast(error.message, true); }
}

function editProduct(id) {
  const product = state.products.find(item => item.id === Number(id));
  $('#editingProductId').value = product.id;
  $('#productName').value = product.name;
  $('#productPrice').value = product.price;
  $('#productFormTitle').textContent = '상품 수정';
  $('#productSubmit').textContent = '변경사항 저장';
  $('#cancelEdit').classList.remove('hidden');
  $('#productName').focus();
}

function resetProductForm() {
  $('#productForm').reset();
  $('#editingProductId').value = '';
  $('#productFormTitle').textContent = '새 상품 등록';
  $('#productSubmit').textContent = '상품 등록';
  $('#cancelEdit').classList.add('hidden');
}

async function deleteProduct(id) {
  const product = state.products.find(item => item.id === Number(id));
  if (!confirm(`'${product.name}' 상품을 삭제할까요?`)) return;
  try {
    await api(`/api/products/${id}`, { method: 'DELETE' });
    toast('상품을 삭제했습니다.');
    await loadProducts();
  } catch (error) { toast(error.message, true); }
}

function openProfile() {
  $('#profileCustomerId').value = state.customer.customerId;
  $('#profileName').value = state.customer.name;
  $('#profilePoint').value = state.customer.point;
  $('#profileDialog').showModal();
}

async function submitProfile(event) {
  event.preventDefault();
  try {
    state.customer = await api('/api/customers', { method: 'PUT', body: JSON.stringify({ customerId: state.customer.customerId, name: $('#profileName').value.trim(), point: Number($('#profilePoint').value) }) });
    renderUser();
    $('#profileDialog').close();
    toast('고객 정보를 수정했습니다.');
  } catch (error) { toast(error.message, true); }
}

function showView(name) {
  document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
  document.querySelectorAll('.nav-link').forEach(link => link.classList.toggle('active', link.dataset.view === name));
  $(`#${name}View`).classList.add('active');
  if (name === 'orders') loadOrders();
  if (name === 'admin') renderAdminProducts();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.querySelectorAll('.nav-link').forEach(link => link.addEventListener('click', () => showView(link.dataset.view)));
document.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => $(`#${button.dataset.close}`).close()));
document.querySelectorAll('dialog').forEach(dialog => dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); }));
$('#authForm').addEventListener('submit', submitAuth);
$('#orderForm').addEventListener('submit', submitOrder);
$('#profileForm').addEventListener('submit', submitProfile);
$('#productForm').addEventListener('submit', submitProduct);
$('#orderQuantity').addEventListener('input', updateOrderTotal);
$('#refreshProducts').addEventListener('click', loadProducts);
$('#refreshOrders').addEventListener('click', loadOrders);
$('#cancelEdit').addEventListener('click', resetProductForm);
$('#productGrid').addEventListener('click', event => { const button = event.target.closest('[data-order]'); if (button) openOrder(button.dataset.order); });
$('#adminProductList').addEventListener('click', event => {
  const edit = event.target.closest('[data-edit]');
  const remove = event.target.closest('[data-delete]');
  if (edit) editProduct(edit.dataset.edit);
  if (remove) deleteProduct(remove.dataset.delete);
});
$('#orderContent').addEventListener('click', event => {
  const button = event.target.closest('[data-cancel-order]');
  if (button) cancelOrder(button.dataset.cancelOrder, Number(button.dataset.max));
});

bindAuthButtons();
loadProducts();
restoreSession();
