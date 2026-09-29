import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import Pagination from '../components/Pagination'
import { formatPrice } from '../format'
import { useProducts } from '../hooks/useProducts'

// The backend caps size at 100.
const PAGE_SIZES = [10, 50, 100]
const DEFAULT_PAGE_SIZE = 10

// URL query for a page and size. Defaults are left out to keep URLs short.
function toSearchParams(page: number, size: number) {
  const params: Record<string, string> = {}
  if (page !== 1) params.page = String(page)
  if (size !== DEFAULT_PAGE_SIZE) params.size = String(size)
  return params
}

function ProductListPage() {
  // Page and size live in the URL (?page=2&size=50) so refresh and the back button keep your place.
  // Defaults are left out of the URL; anything invalid falls back to them.
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(1, Number(searchParams.get('page')) || 1)
  const requestedSize = Number(searchParams.get('size'))
  const size = PAGE_SIZES.includes(requestedSize) ? requestedSize : DEFAULT_PAGE_SIZE
  const { data, loading, error } = useProducts(page - 1, size)

  // Ids of the rows whose full name is shown. Keyed by id (not row index) so a row stays open across pages.
  const [expanded, setExpanded] = useState<Set<number>>(() => new Set())

  function toggleExpanded(id: number) {
    setExpanded((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  // Past the last page (a hand-edited URL, or an old link after products were removed):
  // move to the last page. `replace` keeps the invalid URL out of history, so Back still works.
  // Only once the current request has finished, since `data` may still be the previous page's.
  const lastPage = data && !loading ? data.totalPages : 0
  useEffect(() => {
    if (lastPage > 0 && page > lastPage) {
      setSearchParams(toSearchParams(lastPage, size), { replace: true })
    }
  }, [lastPage, page, size, setSearchParams])

  function navigate(nextPage: number, nextSize: number) {
    setSearchParams(toSearchParams(nextPage, nextSize))
  }

  if (error) return <p className="error">{error}</p>
  if (!data) return <p>Loading…</p>

  if (data.totalElements === 0) {
    return <p className="muted">No products yet.</p>
  }

  return (
    <section className={loading ? 'loading' : undefined}>
      <div className="list-toolbar">
        <label className="page-size">
          Show{' '}
          {/* A new size changes what each page number means, so start again from page 1. */}
          <select value={size} onChange={(e) => navigate(1, Number(e.target.value))}>
            {PAGE_SIZES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>{' '}
          per page
        </label>
        <span className="muted">
          {data.totalElements} {data.totalElements === 1 ? 'product' : 'products'}
        </span>
      </div>

      {data.content.length === 0 ? (
        <p className="muted">No products on this page.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th className="price">Price</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>
                  {/* A button rather than a clickable cell, so it works with the keyboard and screen readers. */}
                  <button
                    type="button"
                    className={expanded.has(p.id) ? 'name-toggle expanded' : 'name-toggle truncate'}
                    aria-expanded={expanded.has(p.id)}
                    title={expanded.has(p.id) ? undefined : p.name}
                    onClick={() => toggleExpanded(p.id)}
                  >
                    {p.name}
                  </button>
                </td>
                <td className="price">{formatPrice(p.itemPrice)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Pagination page={page} totalPages={data.totalPages} onChange={(next) => navigate(next, size)} />
    </section>
  )
}

export default ProductListPage
