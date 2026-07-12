export function formatDateTime(value) {
  if (value == null || value === '') return ''
  if (typeof value === 'string') return value.replace('T', ' ').replace(/\.\d+$/, '')
  return String(value)
}

export function normalizeListDates(list, fields = []) {
  if (!Array.isArray(list)) return list
  return list.map(row => {
    const item = { ...row }
    fields.forEach(f => {
      if (item[f] != null) item[f] = formatDateTime(item[f])
    })
    return item
  })
}
