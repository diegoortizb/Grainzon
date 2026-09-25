import { useEffect, useState } from 'react'
import { listProducts } from '../api/products'
import type { Page, Product } from '../types/product'

type Result = {
  key: string // which page/size this result belongs to
  data: Page<Product> | null
  error: string | null
}

// Loads one page of products and reloads whenever the page number changes.
// While a new page loads, the previous page's data is kept so the table doesn't flicker.
export function useProducts(page: number, size: number) {
  const key = `${page}:${size}`
  const [result, setResult] = useState<Result>({ key: '', data: null, error: null })

  useEffect(() => {
    // Ignore a slow response if the user has already moved to another page.
    let ignore = false

    listProducts(page, size)
      .then((data) => {
        if (!ignore) setResult({ key, data, error: null })
      })
      .catch((e: Error) => {
        if (!ignore) setResult({ key, data: null, error: e.message })
      })

    return () => {
      ignore = true
    }
  }, [key, page, size])

  return {
    data: result.data,
    error: result.key === key ? result.error : null,
    loading: result.key !== key,
  }
}
