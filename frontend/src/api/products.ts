// All HTTP calls to /api/products live here, so components never call fetch directly.
import type { Page, Product } from '../types/product'

// Error bodies are problem+json (RFC 9457). Validation errors also carry a per-field `errors` map.
type Problem = {
  detail?: string
  errors?: Record<string, string>
}

// Best available message for a failed response: field errors, then the detail, then a generic fallback.
async function errorMessage(res: Response, action: string): Promise<string> {
  const problem: Problem | null = await res.json().catch(() => null)
  const fieldErrors = Object.entries(problem?.errors ?? {}).map(([field, message]) => `${field} ${message}`)
  if (fieldErrors.length > 0) return `Couldn't ${action}: ${fieldErrors.join('; ')}.`
  if (problem?.detail) return `Couldn't ${action}: ${problem.detail}`
  return `Couldn't ${action} (${res.status}).`
}

export async function listProducts(page: number, size: number): Promise<Page<Product>> {
  const res = await fetch(`/api/products?page=${page}&size=${size}`)
  if (!res.ok) throw new Error(await errorMessage(res, 'load products'))
  return res.json()
}

export async function createProduct(name: string, itemPrice: number): Promise<Product> {
  const res = await fetch('/api/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, itemPrice }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, 'create product'))
  return res.json()
}
