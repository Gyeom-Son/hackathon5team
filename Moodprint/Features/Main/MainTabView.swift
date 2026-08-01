import SwiftUI

struct MainTabView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        TabView(selection: $appState.selectedTab) {
            HomeView()
                .tag(MainTab.home)
                .tabItem { Label("홈", systemImage: "house.fill") }
            CollectionView()
                .tag(MainTab.collection)
                .tabItem { Label("도감", systemImage: "square.grid.2x2.fill") }
            RecordsView()
                .tag(MainTab.records)
                .tabItem { Label("기록", systemImage: "list.bullet.clipboard.fill") }
        }
        .tint(MoodprintTheme.primary)
    }
}
