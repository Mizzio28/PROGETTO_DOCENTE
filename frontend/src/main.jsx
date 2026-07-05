import { createRoot } from 'react-dom/client';
import CommentiWidget from './CommentiWidget.jsx';

const el = document.getElementById('commenti-root');
if (el) {
  const partitaId = el.dataset.partitaId;
  const userEmail = el.dataset.userEmail || '';
  createRoot(el).render(
    <CommentiWidget partitaId={partitaId} userEmail={userEmail} />
  );
}