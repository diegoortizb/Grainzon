import { useState } from 'react'
import { useSearchParams } from 'react-router'
import Pagination from '../components/Pagination'
import { useProducts } from '../hooks/useProducts'

// The backend caps size at 100.
const PAGE_SIZES = [10, 50, 100]
const DEFAULT_PAGE_SIZE = 10

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

  function navigate(nextPage: number, nextSize: number) {
    const params: Record<string, string> = {}
    if (nextPage !== 1) params.page = String(nextPage)
    if (nextSize !== DEFAULT_PAGE_SIZE) params.size = String(nextSize)
    setSearchParams(params)
  }

  if (error) return <p className="error">{error}</p>
  if (!data) return <p>Loading…</p>

  if (data.totalElements === 0) {
    return <p className="muted">No products yet.</p>
  }

  return (
    <section className={loading ? 'loading' : undefined}>
      {data.content.length === 0 ? (
        <p className="muted">No products on this page.</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
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
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Pagination page={page} totalPages={data.totalPages} onChange={(next) => navigate(next, size)} />

      <div className="list-footer">
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
    </section>
  )
}

export default ProductListPage
