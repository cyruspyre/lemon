#![allow(unsafe_op_in_unsafe_fn)]

use std::{
    fs::read_dir,
    io::{BufRead, BufWriter, Read, StdinLock, Stdout, Write, stdin, stdout},
    time::SystemTime,
};

fn main() {
    let mut stdin = stdin().lock();
    let mut stdout = BufWriter::new(stdout());

    loop {
        let mut typ = [0; 1];

        stdin.read(&mut typ).unwrap();

        if typ[0] == 0 {
            unsafe { open_dir(&mut stdin, &mut stdout).ok() };
            stdout.write(b"\0").unwrap();
        }

        stdout.flush().unwrap();
    }
}

unsafe fn open_dir(
    stdin: &mut StdinLock,
    stdout: &mut BufWriter<Stdout>,
) -> Result<(), Box<dyn std::error::Error>> {
    let mut buf = Vec::new();

    stdin.read_until(b'\0', &mut buf).unwrap();

    let path = str::from_utf8_unchecked(&buf[..buf.len() - 1]);
    let mut iter = read_dir(path)?;

    while let Some(res) = iter.next() {
        let Ok(entry) = res else { continue };
        let Ok(name) = entry.file_name().into_string() else {
            continue;
        };
        let Ok(metadata) = entry.metadata() else {
            continue;
        };

        stdout.write_all(name.as_bytes())?;
        stdout.write_all(b"\0")?;
        stdout.write_all(metadata.len().to_string().as_bytes())?;
        stdout.write_all(b"\0")?;
        stdout.write_all(
            metadata
                .modified()
                .unwrap()
                .duration_since(SystemTime::UNIX_EPOCH)?
                .as_secs()
                .to_string()
                .as_bytes(),
        )?;
        stdout.write_all(&[
            b'\0',
            metadata.is_dir() as u8,
            b'\0',
            metadata.is_symlink() as u8,
            b'\0',
        ])?;
    }

    Ok(())
}
