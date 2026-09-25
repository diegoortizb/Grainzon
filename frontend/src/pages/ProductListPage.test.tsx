import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { listProducts } from '../api/products'
import type { Page, Product } from '../types/product'
import ProductListPage from './ProductListPage'

vi.mock('../api/products')

function page(overrides: Partial<Page<Product>> = {}): Page<Product> {
  return { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, ...overrides }
}

function renderAt(url: string) {
  render(
    <MemoryRouter initialEntries={[url]}>
      <ProductListPage />
    </MemoryRouter>,
  )
}

describe('ProductListPage', () => {
  beforeEach(() => {
    vi.mocked(listProducts).mockReset()
  })

  it('lists the products on the first page', async () => {
    vi.mocked(listProducts).mockResolvedValue(
      page({ content: [{ id: 1, name: 'Hammer' }, { id: 2, name: 'Wrench' }], totalElements: 2, totalPages: 1 }),
    )
    renderAt('/products')

    expect(await screen.findByText('Hammer')).toBeInTheDocument()
    expect(screen.getByText('Wrench')).toBeInTheDocument()
    expect(screen.getByText('2 products')).toBeInTheDocument()
    expect(listProducts).toHaveBeenCalledWith(0, 10)
  })

  it('puts the full name in a tooltip so truncated names can still be read', async () => {
    const name = 'a'.repeat(256)
    vi.mocked(listProducts).mockResolvedValue(page({ content: [{ id: 1, name }], totalElements: 1, totalPages: 1 }))
    renderAt('/products')

    expect(await screen.findByText(name)).toHaveAttribute('title', name)
  })

  it('shows an empty state when there are no products', async () => {
    vi.mocked(listProducts).mockResolvedValue(page())
    renderAt('/products')

    expect(await screen.findByText('No products yet.')).toBeInTheDocument()
  })

  it('shows the error when loading fails', async () => {
    vi.mocked(listProducts).mockRejectedValue(new Error('Failed to load products (500)'))
    renderAt('/products')

    expect(await screen.findByText('Failed to load products (500)')).toBeInTheDocument()
  })

  it('maps the one-based page in the URL to the zero-based API page', async () => {
    vi.mocked(listProducts).mockResolvedValue(page({ page: 1, totalElements: 15, totalPages: 2 }))
    renderAt('/products?page=2')

    await screen.findByText('Page 2 of 2')
    expect(listProducts).toHaveBeenCalledWith(1, 10)
  })

  it.each(['0', '-3', 'abc'])('falls back to the first page for ?page=%s', async (value) => {
    vi.mocked(listProducts).mockResolvedValue(page())
    renderAt(`/products?page=${value}`)

    await screen.findByText('No products yet.')
    expect(listProducts).toHaveBeenCalledWith(0, 10)
  })

  it('loads the next page when Next is clicked', async () => {
    vi.mocked(listProducts).mockImplementation(async (p) =>
      page({ content: [{ id: p + 1, name: `Product on page ${p}` }], page: p, totalElements: 15, totalPages: 2 }),
    )
    renderAt('/products')

    await screen.findByText('Product on page 0')
    await userEvent.click(screen.getByRole('button', { name: /next/i }))

    expect(await screen.findByText('Product on page 1')).toBeInTheDocument()
    expect(listProducts).toHaveBeenLastCalledWith(1, 10)
  })
})
