// All HTTP calls to /api/products live here, so components never call fetch directly.
import type { Page, Product } from '../types/product'

export async function listProducts(page: number, size: number): Promise<Page<Product>> {
  const res = await fetch(`/api/products?page=${page}&size=${size}`)
  if (!res.ok) throw new Error(`Failed to load products (${res.status})`)
  return res.json()
}

export async function createProduct(name: string): Promise<Product> {
  const res = await fetch('/api/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  })
  if (!res.ok) throw new Error(`Failed to create product (${res.status})`)
  return res.json()
}
