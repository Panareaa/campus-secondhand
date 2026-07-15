const STATUS_LABEL: Record<number, string> = {
  0: '待确认',
  1: '已确认',
  2: '已完成',
  3: '已取消',
  4: '已过期',
}

export function tradeStatusLabel(status: number, statusText?: string) {
  return STATUS_LABEL[status] ?? statusText ?? '未知'
}
