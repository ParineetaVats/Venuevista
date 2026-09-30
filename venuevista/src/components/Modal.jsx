export default function Modal({ open, title, children, onClose }) {
  if (!open) return null
  return <div className="modal-bg" onClick={onClose}><div className="modal" role="dialog" aria-modal="true" aria-label={title} onClick={(e) => e.stopPropagation()}><h3>{title}</h3>{children}</div></div>
}
