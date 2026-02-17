import { app } from '../../main/app';
import { config } from '../../main/config';

import { expect } from 'chai';
import nock from 'nock';
import request from 'supertest';

function extractCsrfToken(html: string): string {
  const match = html.match(/name="_csrf"\s+value="([^"]+)"/);
  if (!match || !match[1]) {
    throw new Error('CSRF token not found in response HTML');
  }
  return match[1];
}

function formatCookieHeader(setCookieHeader: string | string[] | undefined): string {
  if (!setCookieHeader) {
    throw new Error('Set-Cookie header missing from response');
  }

  const cookies = Array.isArray(setCookieHeader) ? setCookieHeader : [setCookieHeader];
  if (cookies.length === 0) {
    throw new Error('Set-Cookie header missing from response');
  }

  return cookies.map(cookie => cookie.split(';')[0]).join('; ');
}

describe('Tasks page', () => {
  beforeAll(() => {
    nock.disableNetConnect();
    nock.enableNetConnect('127.0.0.1'); // allow supertest local app calls
  });

  afterEach(() => {
    nock.cleanAll();
  });

  afterAll(() => {
    nock.enableNetConnect();
  });

  test('GET /tasks should render paged tasks from mocked backend', async () => {
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
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
      });

    const res = await request(app).get('/tasks');
    expect(res.status).to.equal(200);
    expect(res.text).to.contain('Task 1');
    expect(scope.isDone()).to.equal(true);
  });

  test('POST /tasks should return 403 when CSRF token is missing', async () => {
    const res = await request(app).post('/tasks').type('form').send({
      title: 'Task without CSRF',
      description: 'Description',
      status: 'PENDING',
      dueDate: '2026-02-14',
    });

    expect(res.status).to.equal(403);
    expect(res.text).to.contain('Something went wrong');
  });

  test('POST /tasks should create task when CSRF token is valid', async () => {
    const apiBaseUrl = new URL(config.apiBaseUrl);
    const createScope = nock(`${apiBaseUrl.protocol}//${apiBaseUrl.host}`)
      .post('/tasks', {
        title: 'CSRF protected task',
        description: 'Created with token',
        status: 'PENDING',
        dueDate: '2026-02-14T00:00:00',
      })
      .reply(201, {
        id: 99,
        title: 'CSRF protected task',
        description: 'Created with token',
        status: 'PENDING',
        dueDate: '2026-02-14T00:00:00',
      });

    const getFormRes = await request(app).get('/tasks/new');
    const csrfToken = extractCsrfToken(getFormRes.text);
    const cookieHeader = formatCookieHeader(getFormRes.headers['set-cookie']);

    const postRes = await request(app).post('/tasks').set('Cookie', cookieHeader).type('form').send({
      _csrf: csrfToken,
      title: 'CSRF protected task',
      description: 'Created with token',
      status: 'PENDING',
      dueDate: '2026-02-14',
    });

    expect(postRes.status).to.equal(302);
    expect(postRes.header.location).to.equal('/tasks');
    expect(createScope.isDone()).to.equal(true);
  });

  test('POST /tasks should return 400 for invalid dueDate even with CSRF token', async () => {
    const getFormRes = await request(app).get('/tasks/new');
    const csrfToken = extractCsrfToken(getFormRes.text);
    const cookieHeader = formatCookieHeader(getFormRes.headers['set-cookie']);

    const postRes = await request(app).post('/tasks').set('Cookie', cookieHeader).type('form').send({
      _csrf: csrfToken,
      title: 'Invalid date task',
      description: 'Bad input',
      status: 'PENDING',
      dueDate: 'not-a-date',
    });

    expect(postRes.status).to.equal(400);
    expect(postRes.text).to.contain('dueDate must be a valid date');
  });

  test('POST /tasks/:id/delete should return 403 when CSRF token is missing', async () => {
    const res = await request(app).post('/tasks/1/delete');

    expect(res.status).to.equal(403);
    expect(res.text).to.contain('Something went wrong');
  });

  test('POST /tasks/:id/delete should delete task when CSRF token is valid', async () => {
    const apiBaseUrl = new URL(config.apiBaseUrl);
    const deleteScope = nock(`${apiBaseUrl.protocol}//${apiBaseUrl.host}`).delete('/tasks/1').reply(204);

    const getFormRes = await request(app).get('/tasks/new');
    const csrfToken = extractCsrfToken(getFormRes.text);
    const cookieHeader = formatCookieHeader(getFormRes.headers['set-cookie']);

    const postRes = await request(app)
      .post('/tasks/1/delete')
      .set('Cookie', cookieHeader)
      .type('form')
      .send({ _csrf: csrfToken });

    expect(postRes.status).to.equal(302);
    expect(postRes.header.location).to.equal('/tasks');
    expect(deleteScope.isDone()).to.equal(true);
  });
});
