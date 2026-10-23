import SwiftUI
import UIKit
import Shared

/// Передаёт открытый извне файл в общий код и защищает от повторной обработки
/// одного и того же URL разными путями доставки (SwiftUI и AppDelegate).
final class FileOpenBridge {
    static let shared = FileOpenBridge()

    private var lastPath: String?
    private var lastHandledAt = Date.distantPast

    func handle(_ url: URL) {
        guard url.isFileURL else { return }
        let path = url.path
        let now = Date()
        if path == lastPath, now.timeIntervalSince(lastHandledAt) < 1.0 {
            return
        }
        lastPath = path
        lastHandledAt = now
        ExternalFileImport.shared.submit(uri: path)
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        FileOpenBridge.shared.handle(url)
        return true
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    FileOpenBridge.shared.handle(url)
                }
        }
    }
}
