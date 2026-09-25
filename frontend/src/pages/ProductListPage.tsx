import { useSearchParams } from 'react-router'
import Pagination from '../components/Pagination'
import { useProducts } from '../hooks/useProducts'

const PAGE_SIZE = 10

function ProductListPage() {
  // The page number lives in the URL (?page=2) so refresh and the back button keep your place.
  const [searchParams, setSearchParams] = useSearchParams()
  const page = Math.max(1, Number(searchParams.get('page')) || 1)
  const { data, loading, error } = useProducts(page - 1, PAGE_SIZE)

  function goToPage(next: number) {
    setSearchParams(next === 1 ? {} : { page: String(next) })
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
                <td className="truncate" title={p.name}>
                  {p.name}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Pagination page={page} totalPages={data.totalPages} onChange={goToPage} />
      <p className="muted count">{data.totalElements} products</p>
    </section>
  )
}

export default ProductListPage
