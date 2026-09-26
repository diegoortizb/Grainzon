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

  it("uses the server's detail when there are no field errors", async () => {
    mockFetch(500, { status: 500, detail: 'Something went wrong.' })

    await expect(listProducts(0, 10)).rejects.toThrow("Couldn't load products: Something went wrong.")
  })

  it('falls back to the status code when the body is not a problem', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('<html>Bad Gateway</html>', { status: 502 }))

    await expect(listProducts(0, 10)).rejects.toThrow("Couldn't load products (502).")
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

  it('explains which field failed and why', async () => {
    mockFetch(400, { status: 400, detail: 'Invalid request.', errors: { name: 'must be at most 256 characters' } })

    await expect(createProduct('a'.repeat(257))).rejects.toThrow(
      "Couldn't create product: name must be at most 256 characters.",
    )
  })

  it('falls back to the status code when the body is empty', async () => {
    mockFetch(400)

    await expect(createProduct('')).rejects.toThrow("Couldn't create product (400).")
  })
})
