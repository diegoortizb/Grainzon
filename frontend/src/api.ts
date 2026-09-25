export type Product = {
  id: number
  name: string
}

export async function listProducts(): Promise<Product[]> {
  const res = await fetch('/api/products')
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
