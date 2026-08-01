import SwiftData

enum MoodprintModelContainer {
    static func make(inMemory: Bool = false) throws -> ModelContainer {
        let schema = Schema([
            MoodRecord.self,
            RecoveryActionRecord.self,
            ActionResultRecord.self,
            RewardRecord.self,
            PetProgressRecord.self
        ])
        let configuration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: inMemory)
        return try ModelContainer(for: schema, configurations: [configuration])
    }
}
