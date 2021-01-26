package example

int counter = 0

fun main(String[] args) {
    schedule {
        task (period=400) {
            println("The count is: " + counter)
        } constrainedBy {
            return counter / 10 % 2 == 0
        } constrainedBy {
            return counter != 10
        } constraintViolation {
            println("Count cannot be 10! (for some reason)")
        }

        task(period=1000) {
            int c1 = counter
            int result = delegate {
                400 + 20 + c1
            }

            println("Result=" + result)
            println(counter + "! = " + factorial(counter))
        } constrainedBy {
            return counter <= 12
        }

        task (period=1000) {
            counter += 1
        }
    }
}

fun factorial(int n): int = if (n <= 1) 1 else n * factorial(n - 1)
