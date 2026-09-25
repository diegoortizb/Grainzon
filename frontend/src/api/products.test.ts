import { describe, expect, it, vi } from 'vitest'
import { createProduct, listProducts } from './products'

function mockFetch(status: number, body: unknown = {}) {
  return vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify(body), { status }))
}

describe('listProducts', () => {
  it('requests the given page and size and returns the JSON', async () => {
    const page = { content: [{ id: 1, name: 'Hammer' }], page: 2, size: 5, totalElements: 11, totalPages: 3 }
    const fetch = mockFetch(200, page)

    await expect(listProducts(2, 5)).resolves.toEqual(page)
    expect(fetch).toHaveBeenCalledWith('/api/products?page=2&size=5')
  })

  it('throws with the status code when the request fails', async () => {
    mockFetch(500)

    await expect(listProducts(0, 10)).rejects.toThrow('Failed to load products (500)')
  })
})

describe('createProduct', () => {
  it('posts the name as JSON and returns the created product', async () => {
    const fetch = mockFetch(201, { id: 7, name: 'Drill' })

    await expect(createProduct('Drill')).resolves.toEqual({ id: 7, name: 'Drill' })
    expect(fetch).toHaveBeenCalledWith('/api/products', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'Drill' }),
    })
  })

  it('throws with the status code when the request fails', async () => {
    mockFetch(400)

    await expect(createProduct('')).rejects.toThrow('Failed to create product (400)')
  })
})
