# frozen_string_literal: true

require 'English'

# Runs command, raising exception on any error.
def exec(cmd)
  puts cmd
  `#{cmd}`
  raise "#{Dir.pwd}: Command '#{cmd}' failed with #{$CHILD_STATUS.exitstatus}" if $CHILD_STATUS.exitstatus != 0
end

require 'timeout'

# Utility class for managing a process.
class MyProc < Data.define(:pid)
  # Checks if process is running.
  # @return [Boolean] true if running
  def running?
    Process.kill(0, pid)
    true
  rescue Errno::ESRCH
    false
  end

  # Awaits for process to terminate cleanly. If it doesn't, the process is killed and an exception is raised.
  def await_shutdown(seconds = 5)
    return unless running?

    puts "#{pid}: awaiting #{seconds}s for process to shut down by itself"
    if wait(seconds)
      puts "#{pid}: exited cleanly"
    else
      puts "#{pid}: wait timed out, terminating"
      kill
      raise 'Timed out waiting for a clean shutdown'
    end
  end

  # Waits for this process to end. Exits immediately if the process is already stopped.
  # @return [Boolean] true if the process is stopped, false if it's still running.
  def wait(seconds = 1)
    return true unless running?

    Timeout.timeout(seconds) do
      Process.wait(pid)
    end
    raise 'unexpected' if running?

    true
  rescue Timeout::Error
    false
  end

  # Sends TERM signal to the process and waits 1s. If nothing happens, process is killed.
  def kill
    return unless running?

    puts "#{pid}: killing via SIGTERM"
    Process.kill('TERM', pid)
    return if wait(1)

    puts "#{pid}: waiting for SIGTERM timed out, sending SIGKILL"
    Process.kill('KILL', pid)
    return if wait(1)

    puts "#{pid}: still running! Giving up." if running?
  end

  # Reads given IO fully and returns {String}. Handles {Errno::EIO} gracefully.
  # @return [String] stdin+stderr
  def read_fully(reader)
    output = []
    begin
      while line = reader.gets
        output << line
      end
    rescue Errno::EIO
      # EIO = normal when child exits
    end
    output.join
  end

  # Sends CTRL+C (SIGINT) to this process.
  def ctrl_c
    Process.kill('INT', pid)
  end
end
