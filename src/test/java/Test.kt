import com.github.bryanser.foxclasses.Skill
import org.junit.Test
import java.lang.invoke.MethodHandles

class Test {

    @Test
    fun test() {
        val m = this::class.java.getDeclaredMethod("target")
        m.isAccessible = true
        val mh = MethodHandles.lookup().unreflect(m)
        mh.invokeWithArguments(this)
    }


    fun target(){
        println("SUCCESS")
    }
}