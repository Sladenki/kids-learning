import { useEffect } from 'react'
import questions from './data/questions.json'
import QuestionCard from './components/QuestionCard'
import Progress from './components/Progress'
import { useQuestionTimer, QUIZ_INTERVAL_MS } from './hooks/useQuestionTimer'
import LockTask from './plugins/LockTask'
import './App.css'

async function checkPendingQuiz(startNewQuiz) {
  const result = await LockTask.getPendingQuizDue()
  if (result.due) {
    startNewQuiz()
  }
}

export default function App() {
  const {
    isLocked,
    correctCount,
    requiredCorrect,
    currentQuestion,
    timeUntilNextFormatted,
    handleCorrectAnswer,
    startNewQuiz,
  } = useQuestionTimer(questions)

  useEffect(() => {
    let listener

    const setup = async () => {
      listener = await LockTask.addListener('quizDue', () => {
        startNewQuiz()
      })
      await checkPendingQuiz(startNewQuiz)
    }

    setup()

    const onVisible = () => {
      if (document.visibilityState === 'visible') {
        checkPendingQuiz(startNewQuiz)
      }
    }

    document.addEventListener('visibilitychange', onVisible)

    return () => {
      listener?.remove()
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [startNewQuiz])

  useEffect(() => {
    if (isLocked) {
      LockTask.cancelQuiz().catch(() => {})
      LockTask.startLockTask().catch(() => {})
    } else {
      LockTask.ensureQuizPermissions().catch(() => {})
      LockTask.scheduleQuiz({ delayMs: QUIZ_INTERVAL_MS }).catch(() => {})
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
          <p className="free-screen__note">
            Можно закрыть приложение — через 10 минут оно откроется само
          </p>
        </main>
      )}
    </div>
  )
}
