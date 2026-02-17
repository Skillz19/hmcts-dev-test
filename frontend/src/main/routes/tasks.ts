import { config } from '../config';

import {
  buildTaskListQueryParams,
  mapTaskPageToViewModel,
  toOptionalBackendDateTime,
  toRequiredBackendDateTime,
} from './tasks.helpers';

import axios, { isAxiosError } from 'axios';
import express, { Express, Request, Response } from 'express';


function getErrorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Unexpected error';
}

export default function (app: Express): void {
  const router = express.Router();

  // INDEX: GET /tasks → list all tasks
  router.get('/', async (req: Request, res: Response) => {
    try {
      const queryParams = buildTaskListQueryParams(req.query as Record<string, unknown>);

      const response = await axios.get(`${config.apiBaseUrl}/tasks`, {
        params: queryParams,
      });

      const viewModel = mapTaskPageToViewModel(response.data, queryParams);
      res.render('tasks/index', viewModel); // renders src/main/views/tasks/index.njk
    } catch (error: unknown) {
      res.status(500).render('error', { message: `Failed to fetch tasks: ${getErrorMessage(error)}` });
    }
  });

  // NEW: GET /tasks/new → form to create a new task
  router.get('/new', (req: Request, res: Response) => {
    res.render('tasks/new'); // renders src/main/views/tasks/new.njk
  });

  // CREATE: POST /tasks → create a new task
  router.post('/', async (req: Request, res: Response) => {
    let dueDate: string;
    try {
      dueDate = toRequiredBackendDateTime(req.body.dueDate);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Invalid dueDate';
      res.status(400).render('error', { message });
      return;
    }

    try {
      const task = {
        title: req.body.title,
        description: req.body.description,
        status: req.body.status,
        dueDate,
      };
      await axios.post(`${config.apiBaseUrl}/tasks`, task);
      res.redirect('/tasks');
    } catch (error: unknown) {
      res.status(500).render('error', { message: `Failed to create task: ${getErrorMessage(error)}` });
    }
  });

  // SHOW: GET /tasks/:id → show a single task
  router.get('/:id', async (req: Request, res: Response) => {
    try {
      const response = await axios.get(`${config.apiBaseUrl}/tasks/${req.params.id}`);
      const task = response.data;
      res.render('tasks/show', { task }); // renders src/main/views/tasks/show.njk
    } catch (error: unknown) {
      res.status(500).render('error', { message: `Failed to fetch task: ${getErrorMessage(error)}` });
    }
  });

  router.get('/:id/edit', async (req: Request, res: Response) => {
    try {
      const response = await axios.get(`${config.apiBaseUrl}/tasks/${req.params.id}`);
      const task = response.data;

      // Format the dueDate for datetime-local input
      const formattedTask = {
        ...task,
        dueDate: new Date(task.dueDate).toISOString().slice(0, 16),
      };

      res.render('tasks/edit', { task: formattedTask });
    } catch (error: unknown) {
      res.status(500).render('error', { message: `Failed to fetch task for editing: ${getErrorMessage(error)}` });
    }
  });

  // UPDATE: POST /tasks/:id/edit → update the task
  router.post('/:id/edit', async (req: Request, res: Response) => {
    let normalizedDueDate: string | undefined;
    try {
      normalizedDueDate = toOptionalBackendDateTime(req.body.dueDate);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Invalid dueDate';
      res.status(400).render('error', { message });
      return;
    }

    try {
      const { title, description, status } = req.body;

      const payload = {
        title,
        description,
        status,
        dueDate: normalizedDueDate,
      };

      await axios.patch(`${config.apiBaseUrl}/tasks/${req.params.id}`, payload);
      res.redirect(`/tasks/${req.params.id}`);
    } catch (error: unknown) {
      res.status(500).render('error', { message: `Failed to update task: ${getErrorMessage(error)}` });
    }
  });

  // DELETE: POST /tasks/:id/delete → delete task
  router.post('/:id/delete', async (req: Request, res: Response) => {
    try {
      await axios.delete(`${config.apiBaseUrl}/tasks/${req.params.id}`);
      res.redirect('/tasks');
    } catch (error: unknown) {
      res.status(500).render('error', { message: `Failed to delete task: ${getErrorMessage(error)}` });
    }
  });

  app.use('/tasks', router);
}
