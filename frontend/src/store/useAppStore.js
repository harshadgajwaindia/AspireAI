import { create } from 'zustand'

export const DEMO_USER_ID = '550e8400-e29b-41d4-a716-446655440000'

function loadJson(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function saveJson(key, value) {
  if (value == null) localStorage.removeItem(key)
  else localStorage.setItem(key, JSON.stringify(value))
}

export const useAppStore = create((set) => ({
  userId: localStorage.getItem('aspire_userId') || DEMO_USER_ID,
  gapReport: loadJson('aspire_gapReport'),
  roadmapPlan: loadJson('aspire_roadmapPlan'),

  setGapReport: (gapReport) => {
    saveJson('aspire_gapReport', gapReport)
    set({ gapReport })
  },

  setRoadmapPlan: (roadmapPlan) => {
    saveJson('aspire_roadmapPlan', roadmapPlan)
    set({ roadmapPlan })
  },

  clearSession: () => {
    saveJson('aspire_gapReport', null)
    saveJson('aspire_roadmapPlan', null)
    set({ gapReport: null, roadmapPlan: null })
  },
}))
