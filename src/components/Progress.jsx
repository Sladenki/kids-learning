export default function Progress({ correct, required }) {
  return (
    <div className="progress" aria-label={`Правильных ответов: ${correct} из ${required}`}>
      {Array.from({ length: required }, (_, i) => (
        <div
          key={i}
          className={`progress__star ${i < correct ? 'progress__star--filled' : ''}`}
          style={{ animationDelay: `${i * 0.1}s` }}
        >
          ★
        </div>
      ))}
    </div>
  )
}
