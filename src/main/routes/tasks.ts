import { Express, Request, Response } from 'express'
import express from 'express'
import axios from 'axios'
import { config } from '../config'

export default function (app: Express) {
  const router = express.Router()

  // INDEX: GET /tasks → list all tasks
  router.get('/', async (req: Request, res: Response) => {
    try {
      const response = await axios.get(`${config.apiBaseUrl}/tasks`)
      const tasks = response.data.map((task: any) => ({
        ...task,
        dueDate: new Date(task.dueDate).toLocaleString('en-GB', {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        })
      }));
      res.render('tasks/index', { tasks }) // renders src/main/views/tasks/index.njk
    } catch (error: any) {
      console.error('Error fetching tasks:', error.message)
      res.status(500).render('error', { message: 'Failed to fetch tasks' })
    }
  })

  // NEW: GET /tasks/new → form to create a new task
  router.get('/new', (req: Request, res: Response) => {
    res.render('tasks/new') // renders src/main/views/tasks/new.njk
  })

  // CREATE: POST /tasks → create a new task
  router.post('/', async (req: Request, res: Response) => {
    try {
      const task = {
        title: req.body.title,
        description: req.body.description,
        status: req.body.status,
        dueDate: req.body.dueDate ? new Date(req.body.dueDate).toISOString() : new Date().toISOString()
      };
      console.log('Creating task:', task)
      await axios.post(`${config.apiBaseUrl}/tasks`, task)
      res.redirect('/tasks')
    } catch (error: any) {
      console.error('Error creating task:', error.message)
      res.status(500).render('error', { message: 'Failed to create task' })
    }
  })

  // SHOW: GET /tasks/:id → show a single task
  router.get('/:id', async (req: Request, res: Response) => {
    try {
      const response = await axios.get(`${config.apiBaseUrl}/tasks/${req.params.id}`)
      const task = response.data
      res.render('tasks/show', { task }) // renders src/main/views/tasks/show.njk
    } catch (error: any) {
      console.error('Error fetching task:', error.message)
      res.status(500).render('error', { message: 'Failed to fetch task' })
    }
  })

  router.get('/:id/edit', async (req: Request, res: Response) => {
    try {
      const response = await axios.get(`${config.apiBaseUrl}/tasks/${req.params.id}`);
      const task = response.data;

      // Format the dueDate for datetime-local input
      console.log('dueDate:', task.dueDate);
      const formattedTask = {
        ...task,
        dueDate: new Date(task.dueDate).toISOString().slice(0, 16)
      };

      res.render('tasks/edit', { task: formattedTask });
    } catch (error: any) {
      console.error('Error fetching task for edit:', error.message);
      res.status(500).render('error', { message: 'Failed to fetch task for editing' });
    }
  });

  // UPDATE: POST /tasks/:id/edit → update the task
  router.post('/:id/edit', async (req: Request, res: Response) => {
    try {
      const { title, description, status, dueDate } = req.body;

      const payload = {
        title,
        description,
        status,
        dueDate: dueDate ? new Date(dueDate).toISOString() : new Date().toISOString()
      };

      await axios.patch(`${config.apiBaseUrl}/tasks/${req.params.id}`, payload);
      res.redirect(`/tasks/${req.params.id}`);
    } catch (error: any) {
      console.error('Error updating task:', error.message);
      res.status(500).render('error', { message: 'Failed to update task' });
    }
  });

  // DELETE: POST /tasks/:id/delete → delete task
  router.post('/:id/delete', async (req: Request, res: Response) => {
    try {
      await axios.delete(`${config.apiBaseUrl}/tasks/${req.params.id}`)
      res.redirect('/tasks')
    } catch (error: any) {
      console.error('Error deleting task:', error.message)
      res.status(500).render('error', { message: 'Failed to delete task' })
    }
  })


  app.use('/tasks', router)
}
