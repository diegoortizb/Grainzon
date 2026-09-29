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
      page({ content: [{ id: 1, name: 'Hammer', itemPrice: 0 }, { id: 2, name: 'Wrench', itemPrice: 0 }], totalElements: 2, totalPages: 1 }),
    )
    renderAt('/products')

    expect(await screen.findByText('Hammer')).toBeInTheDocument()
    expect(screen.getByText('Wrench')).toBeInTheDocument()
    expect(screen.getByText('2 products')).toBeInTheDocument()
    expect(listProducts).toHaveBeenCalledWith(0, 10)
  })

  it('puts the full name in a tooltip so truncated names can still be read', async () => {
    const name = 'a'.repeat(256)
    vi.mocked(listProducts).mockResolvedValue(page({ content: [{ id: 1, name, itemPrice: 0 }], totalElements: 1, totalPages: 1 }))
    renderAt('/products')

    expect(await screen.findByText(name)).toHaveAttribute('title', name)
  })

  it('expands and collapses a name when it is clicked, one row at a time', async () => {
    vi.mocked(listProducts).mockResolvedValue(
      page({ content: [{ id: 1, name: 'Hammer', itemPrice: 0 }, { id: 2, name: 'Wrench', itemPrice: 0 }], totalElements: 2, totalPages: 1 }),
    )
    renderAt('/products')
    const hammer = await screen.findByRole('button', { name: 'Hammer' })
    const wrench = screen.getByRole('button', { name: 'Wrench' })

    expect(hammer).toHaveAttribute('aria-expanded', 'false')
    await userEvent.click(hammer)
    expect(hammer).toHaveAttribute('aria-expanded', 'true')
    expect(hammer).not.toHaveAttribute('title')
    expect(wrench).toHaveAttribute('aria-expanded', 'false')

    await userEvent.click(hammer)
    expect(hammer).toHaveAttribute('aria-expanded', 'false')
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

  it('uses the page size from the URL', async () => {
    vi.mocked(listProducts).mockResolvedValue(page({ size: 50, totalElements: 1, totalPages: 1, content: [{ id: 1, name: 'Hammer', itemPrice: 0 }] }))
    renderAt('/products?size=50')

    await screen.findByText('Hammer')
    expect(listProducts).toHaveBeenCalledWith(0, 50)
    expect(screen.getByRole('combobox')).toHaveValue('50')
  })

  it.each(['7', '1000', 'abc'])('falls back to 10 per page for ?size=%s', async (value) => {
    vi.mocked(listProducts).mockResolvedValue(page({ totalElements: 1, totalPages: 1, content: [{ id: 1, name: 'Hammer', itemPrice: 0 }] }))
    renderAt(`/products?size=${value}`)

    await screen.findByText('Hammer')
    expect(listProducts).toHaveBeenCalledWith(0, 10)
    expect(screen.getByRole('combobox')).toHaveValue('10')
  })

  it('goes back to the first page when the page size changes', async () => {
    vi.mocked(listProducts).mockImplementation(async (p, s) =>
      page({ content: [{ id: 1, name: `Page ${p} size ${s}`, itemPrice: 0 }], page: p, size: s, totalElements: 60, totalPages: Math.ceil(60 / s) }),
    )
    renderAt('/products?page=3')

    await screen.findByText('Page 2 size 10')
    await userEvent.selectOptions(screen.getByRole('combobox'), '50')

    expect(await screen.findByText('Page 0 size 50')).toBeInTheDocument()
    expect(listProducts).toHaveBeenLastCalledWith(0, 50)
  })

  it('keeps the page size when moving between pages', async () => {
    vi.mocked(listProducts).mockImplementation(async (p, s) =>
      page({ content: [{ id: 1, name: `Page ${p} size ${s}`, itemPrice: 0 }], page: p, size: s, totalElements: 60, totalPages: 2 }),
    )
    renderAt('/products?size=50')

    await screen.findByText('Page 0 size 50')
    await userEvent.click(screen.getByRole('button', { name: /next/i }))

    expect(await screen.findByText('Page 1 size 50')).toBeInTheDocument()
  })

  it('moves to the last page when the URL points past it', async () => {
    vi.mocked(listProducts).mockImplementation(async (p, s) =>
      page({ content: p < 25 ? [{ id: p + 1, name: `Product on page ${p}`, itemPrice: 0 }] : [], page: p, size: s, totalElements: 250, totalPages: 25 }),
    )
    renderAt('/products?page=30')

    // Wait for the last page's rows, not the "Page 25 of 25" label: the label already shows while that page is
    // still loading (page from the new URL, total from the previous response), so it doesn't prove the load finished.
    expect(await screen.findByText('Product on page 24')).toBeInTheDocument()
    expect(screen.getByText('Page 25 of 25')).toBeInTheDocument()
    expect(listProducts).toHaveBeenLastCalledWith(24, 10)
  })

  it('keeps the page size when moving back to the last page', async () => {
    vi.mocked(listProducts).mockImplementation(async (p, s) =>
      page({ content: p < 5 ? [{ id: p + 1, name: `Page ${p} size ${s}`, itemPrice: 0 }] : [], page: p, size: s, totalElements: 250, totalPages: 5 }),
    )
    renderAt('/products?page=9&size=50')

    expect(await screen.findByText('Page 4 size 50')).toBeInTheDocument()
    expect(listProducts).toHaveBeenLastCalledWith(4, 50)
  })

  it('loads the next page when Next is clicked', async () => {
    vi.mocked(listProducts).mockImplementation(async (p) =>
      page({ content: [{ id: p + 1, name: `Product on page ${p}`, itemPrice: 0 }], page: p, totalElements: 15, totalPages: 2 }),
    )
    renderAt('/products')

    await screen.findByText('Product on page 0')
    await userEvent.click(screen.getByRole('button', { name: /next/i }))

    expect(await screen.findByText('Product on page 1')).toBeInTheDocument()
    expect(listProducts).toHaveBeenLastCalledWith(1, 10)
  })
})
