import { app } from '../../main/app'
import { expect } from 'chai'
import request from 'supertest'
import nock from 'nock'
import { config } from '../../main/config'

describe('Tasks page', () => {
    beforeAll(() => {
        nock.disableNetConnect()
        nock.enableNetConnect('127.0.0.1'); // allow supertest local app calls
    })

    afterEach(() => {
        nock.cleanAll();
    })

    afterAll(() => {
        nock.enableNetConnect();
    })

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
                        dueDate: '2026-02-14T10:00:00'
                    }
                ],
                page: 0,
                size: 5,
                totalElements: 1,
                totalPages: 1,
                first: true,
                last: true
            });

        const res = await request(app).get('/tasks');
        expect(res.status).to.equal(200);
        expect(res.text).to.contain('Task 1');
        expect(scope.isDone()).to.equal(true);
    });
});
