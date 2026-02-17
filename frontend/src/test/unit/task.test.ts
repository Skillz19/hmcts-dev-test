import {
  buildTaskListQueryParams,
  formatTaskDueDate,
  mapTaskForView,
  mapTaskPageToViewModel,
  toOptionalBackendDateTime,
  toRequiredBackendDateTime,
} from '../../main/routes/tasks.helpers';

describe('tasks route helpers', () => {
  test('buildTaskListQueryParams should return defaults when query is empty', () => {
    const result = buildTaskListQueryParams({});

    expect(result).toEqual({
      page: 0,
      size: 5,
      sortBy: 'id',
      direction: 'desc',
    });
  });

  test('buildTaskListQueryParams should parse valid query values', () => {
    const result = buildTaskListQueryParams({
      page: '2',
      size: '10',
      sortBy: 'title',
      direction: 'desc',
    });

    expect(result).toEqual({
      page: 2,
      size: 10,
      sortBy: 'title',
      direction: 'desc',
    });
  });

  test('buildTaskListQueryParams should fallback for invalid numeric values', () => {
    const result = buildTaskListQueryParams({
      page: 'abc',
      size: '',
      sortBy: 'status',
      direction: 'asc',
    });

    expect(result).toEqual({
      page: 0,
      size: 5,
      sortBy: 'status',
      direction: 'asc',
    });
  });

  test('formatTaskDueDate should format iso date into en-GB text', () => {
    const formatted = formatTaskDueDate('2026-02-14T10:00:00');
    expect(formatted).toContain('2026');
    expect(formatted).toContain(':');
  });

  test('formatTaskDueDate should return original input when date is invalid', () => {
    const formatted = formatTaskDueDate('not-a-date');
    expect(formatted).toBe('not-a-date');
  });

  test('mapTaskForView should format dueDate for display', () => {
    const mapped = mapTaskForView({
      id: 1,
      title: 'Task 1',
      description: 'Desc',
      status: 'PENDING',
      dueDate: '2026-02-14T10:00:00',
    });

    expect(mapped.title).toBe('Task 1');
    expect(mapped.dueDate).not.toBe('2026-02-14T10:00:00');
  });

  test('mapTaskPageToViewModel should map payload and preserve metadata', () => {
    const queryParams = {
      page: 0,
      size: 5,
      sortBy: 'id',
      direction: 'asc',
    };

    const result = mapTaskPageToViewModel(
      {
        items: [{ id: 1, title: 'Task 1', description: 'Desc', status: 'PENDING', dueDate: '2026-02-14T10:00:00' }],
        page: 0,
        size: 5,
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
      },
      queryParams
    );

    expect(result.tasks).toHaveLength(1);
    expect(result.totalElements).toBe(1);
    expect(result.totalPages).toBe(1);
    expect(result.first).toBe(true);
    expect(result.last).toBe(true);
    expect(result.sortBy).toBe('id');
    expect(result.direction).toBe('asc');
  });

  test('mapTaskPageToViewModel should handle empty payload with defaults', () => {
    const queryParams = {
      page: 0,
      size: 5,
      sortBy: 'id',
      direction: 'asc',
    };

    const result = mapTaskPageToViewModel({}, queryParams);

    expect(result.tasks).toEqual([]);
    expect(result.page).toBe(0);
    expect(result.size).toBe(5);
    expect(result.totalElements).toBe(0);
    expect(result.totalPages).toBe(1);
    expect(result.first).toBe(true);
    expect(result.last).toBe(true);
  });

  test('toRequiredBackendDateTime should convert date-only value to LocalDateTime format', () => {
    const result = toRequiredBackendDateTime('2027-12-20');
    expect(result).toBe('2027-12-20T00:00:00');
  });

  test('toRequiredBackendDateTime should add seconds to datetime-local format', () => {
    const result = toRequiredBackendDateTime('2027-12-20T15:45');
    expect(result).toBe('2027-12-20T15:45:00');
  });

  test('toRequiredBackendDateTime should keep full LocalDateTime values unchanged', () => {
    const result = toRequiredBackendDateTime('2027-12-20T15:45:30');
    expect(result).toBe('2027-12-20T15:45:30');
  });

  test('toRequiredBackendDateTime should throw when dueDate is missing', () => {
    expect(() => toRequiredBackendDateTime(undefined)).toThrow('dueDate is required');
  });

  test('toRequiredBackendDateTime should throw when dueDate is invalid', () => {
    expect(() => toRequiredBackendDateTime('not-a-date')).toThrow('dueDate must be a valid date');
  });

  test('toOptionalBackendDateTime should return undefined when dueDate is blank', () => {
    expect(toOptionalBackendDateTime('')).toBeUndefined();
    expect(toOptionalBackendDateTime('   ')).toBeUndefined();
    expect(toOptionalBackendDateTime(undefined)).toBeUndefined();
  });

  test('toOptionalBackendDateTime should parse valid dueDate values', () => {
    expect(toOptionalBackendDateTime('2027-12-20')).toBe('2027-12-20T00:00:00');
    expect(toOptionalBackendDateTime('2027-12-20T15:45')).toBe('2027-12-20T15:45:00');
  });
});
