// Runs the backend Gradle wrapper with the right script for the current OS.
import { spawnSync } from 'node:child_process'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'

const backend = fileURLToPath(new URL('../backend/', import.meta.url))
const isWindows = process.platform === 'win32'
const wrapper = join(backend, isWindows ? 'gradlew.bat' : 'gradlew')

const { status } = spawnSync(isWindows ? `"${wrapper}"` : wrapper, process.argv.slice(2), {
  cwd: backend,
  stdio: 'inherit',
  shell: isWindows,
})
process.exit(status ?? 1)
