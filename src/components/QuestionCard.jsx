import { useState } from 'react'

function setsEqual(a, b) {
  if (a.length !== b.length) return false
  const sortedA = [...a].sort((x, y) => x - y)
  const sortedB = [...b].sort((x, y) => x - y)
  return sortedA.every((val, i) => val === sortedB[i])
}

export default function QuestionCard({ question, onCorrect, onWrong }) {
  const [selected, setSelected] = useState([])
  const [feedback, setFeedback] = useState(null)

  const toggleOption = (index) => {
    if (feedback) return
    setSelected((prev) =>
      prev.includes(index) ? prev.filter((i) => i !== index) : [...prev, index],
    )
  }

  const handleSubmit = () => {
    if (selected.length === 0 || feedback) return

    if (setsEqual(selected, question.correct)) {
      setFeedback('success')
      setTimeout(() => {
        setSelected([])
        setFeedback(null)
        onCorrect()
      }, 800)
    } else {
      setFeedback('error')
      onWrong?.()
      setTimeout(() => setFeedback(null), 600)
    }
  }

  return (
    <div className={`question-card ${feedback ? `question-card--${feedback}` : ''}`}>
      <div className="question-card__letter" key={question.letter}>
        {question.letter}
      </div>

      <h2 className="question-card__title">{question.question}</h2>

      <ul className="question-card__options">
        {question.options.map((option, index) => {
          const isSelected = selected.includes(index)
          return (
            <li key={index}>
              <button
                type="button"
                className={`option-btn ${isSelected ? 'option-btn--selected' : ''}`}
                onClick={() => toggleOption(index)}
                disabled={!!feedback}
                aria-pressed={isSelected}
              >
                <span className="option-btn__check" aria-hidden="true">
                  {isSelected ? '✓' : '☐'}
                </span>
                <span>{option}</span>
              </button>
            </li>
          )
        })}
      </ul>

      <button
        type="button"
        className="submit-btn"
        onClick={handleSubmit}
        disabled={selected.length === 0 || !!feedback}
      >
        Ответить
      </button>

      {feedback === 'success' && (
        <p className="question-card__message question-card__message--success">Молодец!</p>
      )}
      {feedback === 'error' && (
        <p className="question-card__message question-card__message--error">Попробуй ещё!</p>
      )}
    </div>
  )
}
