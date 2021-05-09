package domainmodel.user

import com.harada.domainmodel.user.Mail
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MailTest {
    @Test
    fun `メールアドレスのフォーマットが有効かを確認することができる`() {
       assertTrue(Mail("test@gmail.com").isValid() )
    }
    @Test
    fun `メールアドレスのフォーマットが無効かを確認することができる`() {
        assertTrue(Mail("wrong-format").isInValid() )
    }
}