import { useState, useEffect, useCallback, useRef } from 'react'

export const QUIZ_INTERVAL_MS = 10 * 60 * 1000
export const REQUIRED_CORRECT = 2

function shuffle(array) {
  const copy = [...array]
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[copy[i], copy[j]] = [copy[j], copy[i]]
  }
  return copy
}

function formatTime(ms) {
  const totalSeconds = Math.ceil(ms / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

export function useQuestionTimer(questions) {
  const [isLocked, setIsLocked] = useState(true)
  const [correctCount, setCorrectCount] = useState(0)
  const [quizQuestions, setQuizQuestions] = useState(() => shuffle(questions))
  const [currentIndex, setCurrentIndex] = useState(0)
  const [timeUntilNext, setTimeUntilNext] = useState(QUIZ_INTERVAL_MS)
  const unlockTimeRef = useRef(null)

  const startNewQuiz = useCallback(() => {
    setQuizQuestions(shuffle(questions))
    setCurrentIndex(0)
    setCorrectCount(0)
    setIsLocked(true)
    unlockTimeRef.current = null
    setTimeUntilNext(QUIZ_INTERVAL_MS)
  }, [questions])

  const handleCorrectAnswer = useCallback(() => {
    setCorrectCount((prev) => {
      const next = prev + 1
      if (next >= REQUIRED_CORRECT) {
        setIsLocked(false)
        unlockTimeRef.current = Date.now()
        return next
      }
      setCurrentIndex((i) => (i + 1) % quizQuestions.length)
      return next
    })
  }, [quizQuestions.length])

  useEffect(() => {
    if (isLocked) return undefined

    const tick = () => {
      if (!unlockTimeRef.current) return
      const elapsed = Date.now() - unlockTimeRef.current
      const remaining = Math.max(0, QUIZ_INTERVAL_MS - elapsed)
      setTimeUntilNext(remaining)
      if (remaining === 0) {
        startNewQuiz()
      }
    }

    tick()
    const id = setInterval(tick, 1000)
    return () => clearInterval(id)
  }, [isLocked, startNewQuiz])

  const currentQuestion = quizQuestions[currentIndex]

  return {
    isLocked,
    correctCount,
    requiredCorrect: REQUIRED_CORRECT,
    currentQuestion,
    timeUntilNext,
    timeUntilNextFormatted: formatTime(timeUntilNext),
    handleCorrectAnswer,
    startNewQuiz,
  }
}
