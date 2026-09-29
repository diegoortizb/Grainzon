// Display formatting shared by pages.

const currency = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

// 19.9 -> "$19.90"
export function formatPrice(price: number): string {
  return currency.format(price)
}
