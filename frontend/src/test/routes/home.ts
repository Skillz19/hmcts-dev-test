import { app } from '../../main/app';
import { config } from '../../main/config';

import { expect } from 'chai';
import nock from 'nock';
import request from 'supertest';

describe('Home page', () => {
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

  test('GET / should redirect to /tasks', async () => {
    const res = await request(app).get('/');
    expect(res.status).to.equal(302);
    expect(res.header.location).to.equal('/tasks');
  });

  test('GET / should land on tasks page after redirect', async () => {
    const apiBaseUrl = new URL(config.apiBaseUrl);
    const scope = nock(`${apiBaseUrl.protocol}//${apiBaseUrl.host}`)
      .get('/tasks')
      .query({ page: 0, size: 5, sortBy: 'id', direction: 'desc' })
      .reply(200, {
        items: [],
        page: 0,
        size: 5,
        totalElements: 0,
        totalPages: 1,
        first: true,
        last: true
      });

    const res = await request(app).get('/').redirects(1);
    expect(res.status).to.equal(200);
    expect(scope.isDone()).to.equal(true);
  });
});
