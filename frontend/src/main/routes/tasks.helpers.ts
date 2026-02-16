export type TaskListQueryParams = {
  page: number
  size: number
  sortBy: string
  direction: string
}

export type ApiTask = {
  id: number
  title: string
  description?: string
  status: string
  dueDate: string
}

export type TaskPagePayload = {
  items?: ApiTask[]
  page?: number
  size?: number
  totalElements?: number
  totalPages?: number
  first?: boolean
  last?: boolean
}

const DEFAULT_PAGE = 0
const DEFAULT_SIZE = 5
const DEFAULT_SORT_BY = 'id'
const DEFAULT_DIRECTION = 'asc'

function parseInteger(value: unknown, fallback: number): number {
  if (typeof value !== 'string' || value.trim() === '') {
    return fallback
  }

  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) ? parsed : fallback
}

export function buildTaskListQueryParams(query: Record<string, unknown>): TaskListQueryParams {
  return {
    page: parseInteger(query.page, DEFAULT_PAGE),
    size: parseInteger(query.size, DEFAULT_SIZE),
    sortBy: typeof query.sortBy === 'string' && query.sortBy.trim() !== '' ? query.sortBy : DEFAULT_SORT_BY,
    direction:
      typeof query.direction === 'string' && query.direction.trim() !== '' ? query.direction : DEFAULT_DIRECTION
  }
}

export function formatTaskDueDate(dueDate: string): string {
  const parsed = new Date(dueDate)
  if (Number.isNaN(parsed.getTime())) {
    return dueDate
  }

  return parsed.toLocaleString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function mapTaskForView(task: ApiTask): ApiTask {
  return {
    ...task,
    dueDate: formatTaskDueDate(task.dueDate)
  }
}

export function mapTaskPageToViewModel(
  payload: TaskPagePayload,
  queryParams: TaskListQueryParams
): {
  tasks: ApiTask[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  sortBy: string
  direction: string
} {
  const tasks = (payload.items ?? []).map(mapTaskForView)

  return {
    tasks,
    page: payload.page ?? 0,
    size: payload.size ?? queryParams.size,
    totalElements: payload.totalElements ?? tasks.length,
    totalPages: payload.totalPages ?? 1,
    first: payload.first ?? true,
    last: payload.last ?? true,
    sortBy: queryParams.sortBy,
    direction: queryParams.direction
  }
}
