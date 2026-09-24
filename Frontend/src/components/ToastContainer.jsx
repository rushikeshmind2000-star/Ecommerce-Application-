import { useApp } from '../context/AppContext';
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-react';

const icons = {
  success: <CheckCircle size={18} color="var(--success)" />,
  error:   <XCircle    size={18} color="var(--danger)"  />,
  info:    <Info       size={18} color="var(--primary)" />,
  warning: <AlertTriangle size={18} color="var(--warning)" />,
};

export default function ToastContainer() {
  const { toasts } = useApp();
  return (
    <div className="toast-container">
      {toasts.map(t => (
        <div key={t.id} className={`toast ${t.type}`}>
          {icons[t.type]}
          <span className="toast-message">{t.msg}</span>
        </div>
      ))}
    </div>
  );
}
