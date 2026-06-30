import { registerPlugin } from '@capacitor/core'

const LockTask = registerPlugin('LockTask', {
  web: () => import('./web.js').then((m) => new m.LockTaskWeb()),
})

export default LockTask
