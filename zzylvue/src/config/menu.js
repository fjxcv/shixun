export const topMenus = [
  {
    module: 'workbench',
    name: '工作台',
    path: '/Workbench',
    children: []
  },
  {
    module: 'visit',
    name: '来访管理',
    path: '/ReservationRegister',
    children: [
      { name: '预约登记', path: '/ReservationRegister' },
      { name: '来访登记', path: '/VisitRegister' }
    ]
  },
  {
    module: 'checkinout',
    name: '入退管理',
    path: '/CheckinProcess',
    children: [
      {
        name: '入住管理',
        children: [
          { name: '入住办理', path: '/CheckinProcess' },
          { name: '入住申请', path: '/CheckinApply' }
        ]
      },
      {
        name: '退住管理',
        children: [
          { name: '退住办理', path: '/CheckoutProcess' },
          { name: '退住申请', path: '/CheckoutApply' }
        ]
      }
    ]
  },
  {
    module: 'resident',
    name: '在住管理',
    path: '/ContractTrack',
    children: [
      {
        name: '合同管理',
        children: [
          { name: '合同跟踪', path: '/ContractTrack' }
        ]
      },
      {
        name: '床位管理',
        children: [
          { name: '床位房型', path: '/BedRoomType' },
          { name: '智能床位', path: '/SmartBed' },
          { name: '房型设置', path: '/RoomTypeSetting' }
        ]
      },
      {
        name: '请假管理',
        children: [
          { name: '请假管理', path: '/LeaveManage' }
        ]
      }
    ]
  },
  {
    module: 'customer',
    name: '客户管理',
    path: '/CustomerInfo',
    children: [
      { name: '客户信息', path: '/CustomerInfo' }
    ]
  },
  {
    module: 'collab',
    name: '协同工作',
    path: '/MyTodo',
    children: [
      { name: '我的待办', path: '/MyTodo' },
      { name: '我的申请', path: '/MyApply' }
    ]
  },
  {
    module: 'profile',
    name: '个人中心',
    path: '/UserInfo',
    children: [
      { name: '个人信息', path: '/UserInfo' }
    ]
  }
]

export function findModuleByPath(path) {
  for (const menu of topMenus) {
    if (menu.path === path) return menu.module
    const flat = flattenChildren(menu.children)
    if (flat.some(c => c.path === path)) return menu.module
  }
  if (path.startsWith('/ContractDetail') || path.startsWith('/LeaveDetail')) return 'resident'
  if (path.startsWith('/Checkout')) return 'checkinout'
  if (path.startsWith('/Checkin')) return 'checkinout'
  if (path.startsWith('/My')) return 'collab'
  return 'workbench'
}

function flattenChildren(children = []) {
  const result = []
  children.forEach(item => {
    if (item.path) result.push(item)
    if (item.children) result.push(...flattenChildren(item.children))
  })
  return result
}

export function getSideMenus(module) {
  const menu = topMenus.find(m => m.module === module)
  return menu ? menu.children : []
}
