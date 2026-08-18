import Foundation

@MainActor
struct RecommendationService {
    func recommend(
        emotions: [MoodEmotion],
        energy: MoodEnergy,
        actions: [RecoveryActionRecord],
        history: [ActionResultRecord]
    ) -> [ActionRecommendation] {
        guard (1...3).contains(emotions.count) else { return [] }

        let selectedEmotions = Set(emotions)
        let historyByAction = Dictionary(grouping: history, by: \.actionID)

        return actions
            .map { action in
                let actionHistory = historyByAction[action.id, default: []]
                let emotionMatchCount = selectedEmotions
                    .intersection(action.supportedEmotions)
                    .count
                let energyMatches = action.supportedEnergies.contains(energy)
                let completedCount = actionHistory.count
                let recordedChanges = actionHistory.compactMap(\.change)
                let averageChange = recordedChanges.isEmpty
                    ? 0
                    : recordedChanges.map(\.recommendationWeight).reduce(0, +)
                        / Double(recordedChanges.count)

                let score =
                    Double(emotionMatchCount) * 10
                    + (energyMatches ? 3 : 0)
                    + min(Double(completedCount), 5) * 0.4
                    + averageChange * 2

                return RankedRecommendation(
                    recommendation: ActionRecommendation(
                        action: action,
                        score: score,
                        reason: reason(
                            emotionMatchCount: emotionMatchCount,
                            energyMatches: energyMatches,
                            actionHistory: actionHistory
                        )
                    ),
                    emotionMatchCount: emotionMatchCount,
                    energyMatches: energyMatches,
                    completedCount: completedCount
                )
            }
            .sorted {
                if $0.recommendation.score != $1.recommendation.score {
                    return $0.recommendation.score > $1.recommendation.score
                }
                if $0.emotionMatchCount != $1.emotionMatchCount {
                    return $0.emotionMatchCount > $1.emotionMatchCount
                }
                if $0.energyMatches != $1.energyMatches {
                    return $0.energyMatches
                }
                if $0.completedCount != $1.completedCount {
                    return $0.completedCount > $1.completedCount
                }
                return $0.recommendation.action.catalogOrder
                    < $1.recommendation.action.catalogOrder
            }
            .map(\.recommendation)
    }

    private func reason(
        emotionMatchCount: Int,
        energyMatches: Bool,
        actionHistory: [ActionResultRecord]
    ) -> String {
        var inputReasons: [String] = []
        if emotionMatchCount > 0 {
            inputReasons.append("선택한 감정 \(emotionMatchCount)개")
        }
        if energyMatches {
            inputReasons.append("지금 선택한 에너지")
        }

        let inputText = inputReasons.isEmpty
            ? "지금 입력한 상태에서 부담이 적은 행동"
            : "\(inputReasons.joined(separator: "과 "))에 맞는 행동"

        // 기록이 없는 신규 사용자에게 개인 기록에 근거한 것처럼 말하지 않는다.
        guard !actionHistory.isEmpty else {
            return "\(inputText)으로 추천했어요."
        }

        let positiveCount = actionHistory.compactMap(\.change).filter {
            $0 == .better || $0 == .muchBetter
        }.count
        if positiveCount > 0 {
            return "\(inputText)이고, 이전 기록에서 \(positiveCount)번 도움이 되었다고 남겼어요."
        }
        return "\(inputText)이고, 이전에 완료한 행동이에요."
    }
}

private struct RankedRecommendation {
    let recommendation: ActionRecommendation
    let emotionMatchCount: Int
    let energyMatches: Bool
    let completedCount: Int
}
