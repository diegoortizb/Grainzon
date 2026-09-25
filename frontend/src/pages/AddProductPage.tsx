import { useRef, useState, type FormEvent } from 'react'
import { createProduct } from '../api/products'

function AddProductPage() {
  const [name, setName] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [added, setAdded] = useState<string | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!name.trim()) return
    setSaving(true)
    setError(null)
    setAdded(null)
    try {
      const created = await createProduct(name.trim())
      // Stay on this tab: clear the form so the next product can be typed straight away.
      setAdded(created.name)
      setName('')
      inputRef.current?.focus()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="name">Name</label>
      <div className="row">
        <input
          id="name"
          ref={inputRef}
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g. Cordless drill"
          autoFocus
          required
        />
        <button type="submit" disabled={saving || !name.trim()}>
          {saving ? 'Adding…' : 'Add'}
        </button>
      </div>

      {added && <p className="success">Added “{added}”.</p>}
      {error && <p className="error">{error}</p>}
    </form>
  )
}

export default AddProductPage
