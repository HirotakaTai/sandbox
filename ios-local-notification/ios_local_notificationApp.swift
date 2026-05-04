//
//  ios_local_notificationApp.swift
//  ios-local-notification
//
//  Created by Hirotaka Tai on 2026/05/04.
//

import SwiftUI

@main
struct ios_local_notificationApp: App {
    @StateObject private var notificationManager = LocalNotificationManager()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(notificationManager)
        }
    }
}
