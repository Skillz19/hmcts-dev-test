import { app } from '../../main/app';
import { config } from '../../main/config';

import { expect } from 'chai';
import nock from 'nock';
import request from 'supertest';

describe('Tasks accessibility basics', () => {
  beforeAll(() => {
    nock.disableNetConnect();
    nock.enableNetConnect('127.0.0.1');
  });

  afterEach(() => {
    nock.cleanAll();
  });

  afterAll(() => {
    nock.enableNetConnect();
  });

  test('GET /tasks/new should render labelled form controls and visible page heading', async () => {
    const res = await request(app).get('/tasks/new');

    expect(res.status).to.equal(200);
    expect(res.text).to.contain('<h1 class="govuk-heading-l">Create New Task</h1>');
    expect(res.text).to.contain('label class="govuk-label" for="title"');
    expect(res.text).to.contain('id="title"');
    expect(res.text).to.contain('label class="govuk-label" for="description"');
    expect(res.text).to.contain('id="description"');
    expect(res.text).to.contain('label class="govuk-label" for="status"');
    expect(res.text).to.contain('id="status"');
    expect(res.text).to.contain('label class="govuk-label" for="dueDate"');
    expect(res.text).to.contain('id="dueDate"');
  });

  test('GET /tasks should render table headers and pagination landmark label', async () => {
    const apiBaseUrl = new URL(config.apiBaseUrl);
    const scope = nock(`${apiBaseUrl.protocol}//${apiBaseUrl.host}`)
      .get('/tasks')
      .query({ page: 0, size: 5, sortBy: 'id', direction: 'desc' })
      .reply(200, {
        items: [
          {
            id: 1,
            title: 'Task 1',
            description: 'Desc',
            status: 'PENDING',
            dueDate: '2026-02-14T10:00:00',
          },
        ],
        page: 0,
        size: 5,
        totalElements: 2,
        totalPages: 2,
        first: true,
        last: false,
      });

    const res = await request(app).get('/tasks');

    expect(res.status).to.equal(200);
    expect(res.text).to.contain('<th class="govuk-table__header">Title</th>');
    expect(res.text).to.contain('<th class="govuk-table__header">Description</th>');
    expect(res.text).to.contain('<th class="govuk-table__header">Status</th>');
    expect(res.text).to.contain('<th class="govuk-table__header">Due Date</th>');
    expect(res.text).to.contain('aria-label="Task pagination"');
    expect(scope.isDone()).to.equal(true);
  });

  test('GET /tasks/:id/edit should render labelled editable controls', async () => {
    const apiBaseUrl = new URL(config.apiBaseUrl);
    const scope = nock(`${apiBaseUrl.protocol}//${apiBaseUrl.host}`).get('/tasks/7').reply(200, {
      id: 7,
      title: 'Editable Task',
      description: 'Editable description',
      status: 'PENDING',
      dueDate: '2026-02-14T10:30:00',
    });

    const res = await request(app).get('/tasks/7/edit');

    expect(res.status).to.equal(200);
    expect(res.text).to.contain('<h1 class="govuk-heading-l">Edit Task</h1>');
    expect(res.text).to.contain('label class="govuk-label" for="title"');
    expect(res.text).to.contain('label class="govuk-label" for="description"');
    expect(res.text).to.contain('label class="govuk-label" for="status"');
    expect(res.text).to.contain('label class="govuk-label" for="dueDate"');
    expect(scope.isDone()).to.equal(true);
  });
});
