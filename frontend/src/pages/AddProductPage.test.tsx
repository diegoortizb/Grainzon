import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createProduct } from '../api/products'
import { PRODUCT_NAME_MAX_LENGTH } from '../types/product'
import AddProductPage from './AddProductPage'

vi.mock('../api/products')

describe('AddProductPage', () => {
  beforeEach(() => {
    vi.mocked(createProduct).mockReset()
  })

  it('disables Add until a non-blank name is typed', async () => {
    render(<AddProductPage />)
    const add = screen.getByRole('button', { name: 'Add' })

    expect(add).toBeDisabled()
    await userEvent.type(screen.getByLabelText('Name'), '   ')
    expect(add).toBeDisabled()
    await userEvent.type(screen.getByLabelText('Name'), 'Drill')
    expect(add).toBeEnabled()
  })

  it('creates the trimmed name, confirms it and clears the form', async () => {
    vi.mocked(createProduct).mockResolvedValue({ id: 7, name: 'Drill', itemPrice: 0 })
    render(<AddProductPage />)
    const input = screen.getByLabelText('Name')

    await userEvent.type(input, '  Drill  {Enter}')

    expect(createProduct).toHaveBeenCalledWith('Drill', 0)
    expect(await screen.findByText('Added “Drill” at $0.00.')).toBeInTheDocument()
    expect(input).toHaveValue('')
    expect(input).toHaveFocus()
  })

  it('creates the product with the typed price and clears the price', async () => {
    vi.mocked(createProduct).mockResolvedValue({ id: 7, name: 'Drill', itemPrice: 19.99 })
    render(<AddProductPage />)
    const price = screen.getByLabelText('Price (USD)')

    await userEvent.type(screen.getByLabelText('Name'), 'Drill')
    await userEvent.type(price, '19.99{Enter}')

    expect(createProduct).toHaveBeenCalledWith('Drill', 19.99)
    expect(await screen.findByText('Added “Drill” at $19.99.')).toBeInTheDocument()
    expect(price).toHaveValue(null)
  })

  it('disables Add and explains why when the price is negative', async () => {
    render(<AddProductPage />)
    const price = screen.getByLabelText('Price (USD)')

    await userEvent.type(screen.getByLabelText('Name'), 'Drill')
    expect(price).toHaveAccessibleDescription('Optional. Leave empty for $0.00.')
    await userEvent.type(price, '-5')

    expect(screen.getByRole('button', { name: 'Add' })).toBeDisabled()
    expect(price).toHaveAccessibleDescription('Price must be 0 or more.')
    expect(price).toBeInvalid()
  })

  it('uses a plain message when the browser rejects the price, and clears it on typing', async () => {
    render(<AddProductPage />)
    const price = screen.getByLabelText<HTMLInputElement>('Price (USD)')

    fireEvent.invalid(price)
    expect(price.validationMessage).toBe('Please enter a valid value')

    await userEvent.type(price, '1')
    expect(price.validationMessage).toBe('')
  })

  it('shows the error and keeps the name when creating fails', async () => {
    vi.mocked(createProduct).mockRejectedValue(new Error('Failed to create product (500)'))
    render(<AddProductPage />)
    const input = screen.getByLabelText('Name')

    await userEvent.type(input, 'Drill{Enter}')

    expect(await screen.findByText('Failed to create product (500)')).toBeInTheDocument()
    expect(input).toHaveValue('Drill')
  })

  it('shows the character limit and how much of it is used', async () => {
    render(<AddProductPage />)
    const input = screen.getByLabelText('Name')

    expect(input).toHaveAccessibleDescription(`0 / ${PRODUCT_NAME_MAX_LENGTH} characters`)
    expect(input).toHaveAttribute('title', `Up to ${PRODUCT_NAME_MAX_LENGTH} characters`)
    await userEvent.type(input, 'Drill')
    expect(input).toHaveAccessibleDescription(`5 / ${PRODUCT_NAME_MAX_LENGTH} characters`)
  })

  it('stops typing at the maximum name length', async () => {
    render(<AddProductPage />)
    const input = screen.getByLabelText('Name')

    await userEvent.type(input, 'a'.repeat(PRODUCT_NAME_MAX_LENGTH + 5))

    expect(input).toHaveAttribute('maxLength', String(PRODUCT_NAME_MAX_LENGTH))
    expect(input).toHaveValue('a'.repeat(PRODUCT_NAME_MAX_LENGTH))
  })
})
