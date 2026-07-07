export class LockTaskWeb {
  async startLockTask() {
    console.info('[LockTask] startLockTask — web stub')
  }

  async stopLockTask() {
    console.info('[LockTask] stopLockTask — web stub')
  }

  async scheduleQuiz() {
    console.info('[LockTask] scheduleQuiz — web stub')
  }

  async cancelQuiz() {}

  async getPendingQuizDue() {
    return { due: false }
  }

  async ensureQuizPermissions() {}
}
