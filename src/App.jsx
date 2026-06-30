import { useEffect } from 'react'
import questions from './data/questions.json'
import QuestionCard from './components/QuestionCard'
import Progress from './components/Progress'
import { useQuestionTimer } from './hooks/useQuestionTimer'
import LockTask from './plugins/LockTask'
import './App.css'

export default function App() {
  const {
    isLocked,
    correctCount,
    requiredCorrect,
    currentQuestion,
    timeUntilNextFormatted,
    handleCorrectAnswer,
  } = useQuestionTimer(questions)

  useEffect(() => {
    if (isLocked) {
      LockTask.startLockTask().catch(() => {})
    } else {
      LockTask.stopLockTask().catch(() => {})
    }
  }, [isLocked])

  if (!currentQuestion) {
    return null
  }

  return (
    <div className={`app ${isLocked ? 'app--locked' : 'app--free'}`}>
      {isLocked ? (
        <main className="quiz-screen">
          <Progress correct={correctCount} required={requiredCorrect} />
          <QuestionCard
            key={`${currentQuestion.id}-${correctCount}`}
            question={currentQuestion}
            onCorrect={handleCorrectAnswer}
          />
        </main>
      ) : (
        <main className="free-screen">
          <div className="free-screen__emoji" aria-hidden="true">
            🎉
          </div>
          <h1 className="free-screen__title">Молодец!</h1>
          <p className="free-screen__hint">Можно играть</p>
          <div className="free-screen__timer">
            <span className="free-screen__timer-label">Следующий вопрос через</span>
            <span className="free-screen__timer-value">{timeUntilNextFormatted}</span>
          </div>
        </main>
      )}
    </div>
  )
}
