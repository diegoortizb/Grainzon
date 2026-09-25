// Shapes of the JSON returned by the backend API.

export type Product = {
  id: number
  name: string
}

export type Page<T> = {
  content: T[]
  page: number // zero-based
  size: number
  totalElements: number
  totalPages: number
}
