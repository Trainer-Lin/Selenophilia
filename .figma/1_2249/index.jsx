import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.row}>
      <div className={styles.title2}>
        <p className={styles.title}>Title</p>
        <div className={styles.bG}>
          <img src="../image/mmjdun39-l6em9re.svg" className={styles.iconChevron} />
        </div>
      </div>
      <div className={styles.carousel}>
        <img src="../image/mmjdun3a-xak6x3h.png" className={styles.image} />
        <img src="../image/mmjdun3a-9828y8v.png" className={styles.image} />
        <img src="../image/mmjdun3a-hkj4hai.png" className={styles.image} />
        <img src="../image/mmjdun3a-s9qdn2i.png" className={styles.image} />
      </div>
    </div>
  );
}

export default Component;
