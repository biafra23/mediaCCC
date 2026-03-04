import SwiftUI
import shared
import GoogleCast

@main
struct iOSApp: App {

    init() {
        MainViewControllerKt.doInitKoin()
        
        // Initialize Google Cast
        let discoveryCriteria = GCKDiscoveryCriteria(applicationID: kGCKDefaultMediaReceiverApplicationID)
        let options = GCKCastOptions(discoveryCriteria: discoveryCriteria)
        GCKCastContext.setSharedInstanceWith(options)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
