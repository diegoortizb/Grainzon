import { useRef, useState, type FormEvent } from 'react'
import { createProduct } from '../api/products'
import { formatPrice } from '../format'
import { PRODUCT_NAME_MAX_LENGTH, type Product } from '../types/product'

function AddProductPage() {
  const [name, setName] = useState('')
  // Kept as typed; an empty field means 0, the backend's default.
  const [price, setPrice] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [added, setAdded] = useState<Product | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const itemPrice = price === '' ? 0 : Number(price)
  const priceValid = Number.isFinite(itemPrice) && itemPrice >= 0

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!name.trim() || !priceValid) return
    setSaving(true)
    setError(null)
    setAdded(null)
    try {
      const created = await createProduct(name.trim(), itemPrice)
      // Stay on this tab: clear the form so the next product can be typed straight away.
      setAdded(created)
      setName('')
      setPrice('')
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
          maxLength={PRODUCT_NAME_MAX_LENGTH}
          title={`Up to ${PRODUCT_NAME_MAX_LENGTH} characters`}
          aria-describedby="name-hint"
          autoFocus
          required
        />
      </div>
      <p id="name-hint" className="muted hint">
        {name.length} / {PRODUCT_NAME_MAX_LENGTH} characters
      </p>

      <label htmlFor="price" className="field-label">
        Price (USD)
      </label>
      <div className="row">
        {/* The browser also blocks submitting more than two decimals or text that isn't a number.
            Its own message lists the nearest valid values, so replace it with a plain one;
            typing clears it so the browser can check the new value. */}
        <input
          id="price"
          className="price-input"
          type="number"
          inputMode="decimal"
          min="0"
          step="0.01"
          value={price}
          onChange={(e) => {
            e.target.setCustomValidity('')
            setPrice(e.target.value)
          }}
          onInvalid={(e) => e.currentTarget.setCustomValidity('Please enter a valid value')}
          placeholder="0.00"
          aria-describedby="price-hint"
          aria-invalid={!priceValid}
        />
        <button type="submit" disabled={saving || !name.trim() || !priceValid}>
          {saving ? 'Adding…' : 'Add'}
        </button>
      </div>
      <p id="price-hint" className={priceValid ? 'muted hint' : 'error hint'}>
        {priceValid ? 'Optional. Leave empty for $0.00.' : 'Price must be 0 or more.'}
      </p>

      {added && (
        <p className="success truncate" title={added.name}>
          Added “{added.name}” at {formatPrice(added.itemPrice)}.
        </p>
      )}
      {error && <p className="error">{error}</p>}
    </form>
  )
}

export default AddProductPage
