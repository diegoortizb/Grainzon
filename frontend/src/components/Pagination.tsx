type Props = {
  page: number // one-based, as shown to the user
  totalPages: number
  onChange: (page: number) => void
}

function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null

  return (
    <nav className="pagination" aria-label="Pagination">
      <button onClick={() => onChange(page - 1)} disabled={page <= 1}>
        ← Previous
      </button>
      <span>
        Page {page} of {totalPages}
      </span>
      <button onClick={() => onChange(page + 1)} disabled={page >= totalPages}>
        Next →
      </button>
    </nav>
  )
}

export default Pagination
